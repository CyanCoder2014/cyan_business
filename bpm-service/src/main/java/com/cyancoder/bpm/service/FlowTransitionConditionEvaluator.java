package com.cyancoder.bpm.service;

import com.cyancoder.bpm.domain.ConditionLogicalOperator;
import com.cyancoder.bpm.domain.ConditionOperator;
import com.cyancoder.bpm.domain.FlowCondition;
import com.cyancoder.bpm.domain.FlowTransition;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class FlowTransitionConditionEvaluator {

    public boolean evaluate(FlowTransition transition, Map<String, Object> payload, Map<String, Object> context) {
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
            case STARTS_WITH -> actual instanceof String s && expected != null && s.startsWith(String.valueOf(expected));
            case ENDS_WITH -> actual instanceof String s && expected != null && s.endsWith(String.valueOf(expected));
            case GT -> compareNumbers(actual, expected) > 0;
            case GTE -> compareNumbers(actual, expected) >= 0;
            case LT -> compareNumbers(actual, expected) < 0;
            case LTE -> compareNumbers(actual, expected) <= 0;
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

    private int compareNumbers(Object actual, Object expected) {
        if (!(actual instanceof Number) || !(expected instanceof Number)) {
            return -1;
        }
        return Double.compare(((Number) actual).doubleValue(), ((Number) expected).doubleValue());
    }
}

