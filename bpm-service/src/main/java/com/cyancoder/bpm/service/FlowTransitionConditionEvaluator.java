package com.cyancoder.bpm.service;

import com.cyancoder.bpm.api.dto.TransitionActorContext;
import com.cyancoder.bpm.domain.ConditionLogicalOperator;
import com.cyancoder.bpm.domain.ConditionOperator;
import com.cyancoder.bpm.domain.FlowCondition;
import com.cyancoder.bpm.domain.FlowTransition;
import com.cyancoder.bpm.domain.ManagedObject;
import com.cyancoder.bpm.expression.TransitionAntlrExpressionEvaluator;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;
import java.util.function.IntPredicate;

@Component
public class FlowTransitionConditionEvaluator {
    private final TransitionAntlrExpressionEvaluator antlrExpressionEvaluator = new TransitionAntlrExpressionEvaluator();

    public boolean evaluate(FlowTransition transition, Map<String, Object> payload, Map<String, Object> context) {
        return evaluate(transition, payload, context, null, null);
    }

    public boolean evaluate(FlowTransition transition,
                            Map<String, Object> payload,
                            Map<String, Object> context,
                            ManagedObject object,
                            TransitionActorContext actorContext) {
        if (transition.conditionExpression() != null && !transition.conditionExpression().isBlank()
                && looksLikeAntlrExpression(transition.conditionExpression())) {
            return antlrExpressionEvaluator.evaluate(
                    transition.conditionExpression(),
                    object == null ? syntheticObject(payload) : object,
                    context,
                    actorContext
            );
        }
        List<FlowCondition> conditions = transition.conditions();
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }
        boolean andMode = transition.conditionOperator() == null || transition.conditionOperator() == ConditionLogicalOperator.AND;
        boolean current = andMode;
        for (FlowCondition condition : conditions) {
            boolean result = evaluateCondition(condition, payload, context);
            if (andMode) {
                current = current && result;
                if (!current) {
                    return false;
                }
            } else {
                current = current || result;
                if (current) {
                    return true;
                }
            }
        }
        return current;
    }

    private boolean looksLikeAntlrExpression(String expression) {
        return expression.contains("&&")
                || expression.contains("||")
                || expression.contains("==")
                || expression.contains("!=")
                || expression.contains(" contains ")
                || expression.contains(" between ");
    }

    private ManagedObject syntheticObject(Map<String, Object> payload) {
        ManagedObject object = new ManagedObject();
        object.setPayload(payload);
        return object;
    }

    private boolean evaluateCondition(FlowCondition condition, Map<String, Object> payload, Map<String, Object> context) {
        Object actual = valueForPath(condition.field(), payload, context);
        Object expected = condition.value();
        ConditionOperator operator = condition.operator() == null ? ConditionOperator.EQ : condition.operator();
        return switch (operator) {
            case EQ -> Objects.equals(actual, expected);
            case NE -> !Objects.equals(actual, expected);
            case EXISTS -> actual != null;
            case IS_NULL -> actual == null;
            case NOT_NULL -> actual != null;
            case EMPTY -> isEmpty(actual);
            case NOT_EMPTY -> !isEmpty(actual);
            case IN -> expected instanceof Collection<?> c && c.contains(actual);
            case NOT_IN -> expected instanceof Collection<?> c && !c.contains(actual);
            case CONTAINS -> contains(actual, expected);
            case CONTAINS_ANY -> containsAny(actual, expected);
            case STARTS_WITH -> actual instanceof String s && expected != null && s.startsWith(String.valueOf(expected));
            case ENDS_WITH -> actual instanceof String s && expected != null && s.endsWith(String.valueOf(expected));
            case MATCHES -> expected != null && Pattern.compile(String.valueOf(expected)).matcher(String.valueOf(actual == null ? "" : actual)).find();
            case GT -> compareNumbers(actual, expected) > 0;
            case GTE -> compareNumbers(actual, expected) >= 0;
            case LT -> compareNumbers(actual, expected) < 0;
            case LTE -> compareNumbers(actual, expected) <= 0;
            case BEFORE -> dateComparison(actual, expected, result -> result < 0);
            case AFTER -> dateComparison(actual, expected, result -> result > 0);
            case ON_OR_BEFORE -> dateComparison(actual, expected, result -> result <= 0);
            case ON_OR_AFTER -> dateComparison(actual, expected, result -> result >= 0);
            case BETWEEN -> between(actual, expected);
            case NOT_BETWEEN -> !between(actual, expected);
        };
    }

    private Object valueForPath(String path, Map<String, Object> payload, Map<String, Object> context) {
        if (path == null || path.isBlank()) {
            return null;
        }
        if (path.startsWith("context.")) {
            return nestedRead(context, path.substring("context.".length()));
        }
        return nestedRead(payload, path.startsWith("payload.") ? path.substring("payload.".length()) : path);
    }

    @SuppressWarnings("unchecked")
    private Object nestedRead(Map<String, Object> source, String path) {
        if (source == null) {
            return null;
        }
        Object current = source;
        for (String segment : path.split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = ((Map<String, Object>) map).get(segment);
        }
        return current;
    }

    private boolean contains(Object actual, Object expected) {
        if (actual instanceof Collection<?> collection) {
            return collection.contains(expected);
        }
        if (actual instanceof String s && expected != null) {
            return s.contains(String.valueOf(expected));
        }
        return false;
    }

    private boolean containsAny(Object actual, Object expected) {
        if (!(actual instanceof Collection<?> collection)) {
            return contains(actual, expected);
        }
        if (expected instanceof Collection<?> expectedItems) {
            for (Object item : expectedItems) {
                if (collection.contains(item)) {
                    return true;
                }
            }
            return false;
        }
        return collection.contains(expected);
    }

    private boolean isEmpty(Object actual) {
        if (actual == null) {
            return true;
        }
        if (actual instanceof String s) {
            return s.isBlank();
        }
        if (actual instanceof Collection<?> collection) {
            return collection.isEmpty();
        }
        if (actual instanceof Map<?, ?> map) {
            return map.isEmpty();
        }
        return false;
    }

    private boolean between(Object actual, Object expected) {
        if (!(expected instanceof List<?> bounds) || bounds.size() < 2) {
            return false;
        }
        if (looksLikeDate(actual) || looksLikeDate(bounds.get(0)) || looksLikeDate(bounds.get(1))) {
            Integer lower = compareDates(actual, bounds.get(0));
            Integer upper = compareDates(actual, bounds.get(1));
            return lower != null && upper != null && lower >= 0 && upper <= 0;
        }
        double value = toDouble(actual);
        return value >= toDouble(bounds.get(0)) && value <= toDouble(bounds.get(1));
    }

    private boolean dateComparison(Object actual, Object expected, IntPredicate predicate) {
        Integer result = compareDates(actual, expected);
        return result != null && predicate.test(result);
    }

    private Integer compareDates(Object actual, Object expected) {
        LocalDate left = toLocalDate(actual);
        LocalDate right = toLocalDate(expected);
        if (left == null || right == null) {
            return null;
        }
        return left.compareTo(right);
    }

    private boolean looksLikeDate(Object value) {
        return value instanceof LocalDate
                || value instanceof LocalDateTime
                || value instanceof Instant
                || (value instanceof String text && text.matches("^\\d{4}[-/]\\d{2}[-/]\\d{2}([T ].*)?$|^\\d{8}$|^(today|tomorrow|yesterday|now)$|^[+-]\\d+[dmy]$"));
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toLocalDate();
        }
        if (value instanceof Instant instant) {
            return instant.atZone(ZoneId.systemDefault()).toLocalDate();
        }
        String text = value.toString().trim();
        if (text.isBlank()) {
            return null;
        }
        LocalDate relative = parseRelativeDate(text);
        if (relative != null) {
            return relative;
        }
        try {
            return Instant.parse(text).atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }
        if (text.matches("^\\d{8}$")) {
            try {
                return LocalDate.parse(text, DateTimeFormatter.BASIC_ISO_DATE);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private LocalDate parseRelativeDate(String text) {
        String normalized = text.toLowerCase();
        LocalDate today = LocalDate.now();
        return switch (normalized) {
            case "today" -> today;
            case "tomorrow" -> today.plusDays(1);
            case "yesterday" -> today.minusDays(1);
            case "now" -> ZonedDateTime.now(ZoneId.systemDefault()).toLocalDate();
            default -> parseRelativeOffset(normalized, today);
        };
    }

    private LocalDate parseRelativeOffset(String text, LocalDate base) {
        if (!text.matches("^[+-]\\d+[dmy]$")) {
            return null;
        }
        int amount = Integer.parseInt(text.substring(0, text.length() - 1));
        char unit = text.charAt(text.length() - 1);
        return switch (unit) {
            case 'd' -> base.plusDays(amount);
            case 'm' -> base.plusMonths(amount);
            case 'y' -> base.plusYears(amount);
            default -> null;
        };
    }

    private int compareNumbers(Object actual, Object expected) {
        return Double.compare(toDouble(actual), toDouble(expected));
    }

    private double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value != null) {
            try {
                return Double.parseDouble(String.valueOf(value));
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }
}
