package com.cyancoder.automationorchestrator.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class N8nExpressionService {
    private static final Pattern TEMPLATE = Pattern.compile("\\{\\{\\s*(.+?)\\s*}}", Pattern.DOTALL);
    private static final Pattern NODE_REFERENCE = Pattern.compile("^\\$node\\[(['\"])(.+?)\\1]\\.(?:json\\.)?(.+)$");
    private static final Pattern DOLLAR_NODE_REFERENCE = Pattern.compile("^\\$\\((['\"])(.+?)\\1\\)\\.(?:(?:item|first\\(\\))\\.)?(?:json\\.)?(.+)$");

    public Object materialize(Object value, Evaluation evaluation) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((key, item) -> {
                if (key != null) result.put(key.toString(), materialize(item, evaluation));
            });
            return result;
        }
        if (value instanceof Iterable<?> iterable) {
            List<Object> result = new ArrayList<>();
            iterable.forEach(item -> result.add(materialize(item, evaluation)));
            return result;
        }
        if (!(value instanceof String text)) return value;
        String normalized = text.startsWith("=") ? text.substring(1) : text;
        Matcher matcher = TEMPLATE.matcher(normalized);
        if (matcher.matches()) return evaluate(matcher.group(1), evaluation);
        if (!matcher.find()) return text;
        matcher.reset();
        StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            Object replacement = evaluate(matcher.group(1), evaluation);
            matcher.appendReplacement(output, Matcher.quoteReplacement(Objects.toString(replacement, "")));
        }
        matcher.appendTail(output);
        return output.toString();
    }

    public Object evaluate(String expression, Evaluation evaluation) {
        String value = trimOuterParentheses(expression == null ? "" : expression.trim());
        for (String operator : List.of("??", "||", "&&", "===", "!==", "==", "!=", ">=", "<=", ">", "<", "+", "-", "*", "/")) {
            int split = findOperator(value, operator);
            if (split > 0) {
                Object left = evaluate(value.substring(0, split), evaluation);
                Object right = evaluate(value.substring(split + operator.length()), evaluation);
                return applyOperator(left, operator, right);
            }
        }
        if (value.startsWith("!")) return !truthy(evaluate(value.substring(1), evaluation));
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        if ("true".equals(value)) return true;
        if ("false".equals(value)) return false;
        if ("null".equals(value) || "undefined".equals(value)) return null;
        try {
            if (value.contains(".")) return Double.parseDouble(value);
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return applyStringMethod(resolveReference(value, evaluation), value);
        }
    }

    private Object resolveReference(String expression, Evaluation evaluation) {
        String base = stripMethod(expression);
        if ("$json".equals(base)) return evaluation.json();
        if (base.startsWith("$json.")) return AutomationDataSupport.readPath(evaluation.json(), normalizePath(base.substring(6)));
        if (base.startsWith("$json[")) return AutomationDataSupport.readPath(evaluation.json(), normalizePath(base.substring(5)));
        if ("$binary".equals(base)) return evaluation.binary();
        if (base.startsWith("$binary.")) return AutomationDataSupport.readPath(evaluation.binary(), normalizePath(base.substring(8)));
        if ("$input.item".equals(base)) return evaluation.item();
        if (base.startsWith("$input.item.json.")) return AutomationDataSupport.readPath(evaluation.json(), normalizePath(base.substring(17)));
        if (base.startsWith("$input.first().json.")) return firstJsonPath(evaluation.inputItems(), base.substring(20));
        if ("$input.all()".equals(base)) return evaluation.inputItems();
        if (base.startsWith("$vars.")) return AutomationDataSupport.readPath(evaluation.variables(), normalizePath(base.substring(6)));
        if (base.startsWith("$execution.")) return AutomationDataSupport.readPath(evaluation.execution(), normalizePath(base.substring(11)));
        if (base.startsWith("$workflow.")) return AutomationDataSupport.readPath(evaluation.workflow(), normalizePath(base.substring(10)));
        if ("$itemIndex".equals(base)) return evaluation.itemIndex();
        Matcher node = NODE_REFERENCE.matcher(base);
        if (node.matches()) return nodePath(evaluation, node.group(2), node.group(3));
        Matcher dollarNode = DOLLAR_NODE_REFERENCE.matcher(base);
        if (dollarNode.matches()) return nodePath(evaluation, dollarNode.group(2), dollarNode.group(3));
        return expression;
    }

    private Object nodePath(Evaluation evaluation, String nodeName, String path) {
        Object stored = evaluation.nodeOutputs().get(nodeName);
        List<Object> items = AutomationDataSupport.list(stored);
        if (items.isEmpty()) return null;
        int index = Math.min(evaluation.itemIndex(), items.size() - 1);
        Map<String, Object> item = AutomationDataSupport.map(items.get(index));
        return AutomationDataSupport.readPath(item.getOrDefault("json", item), normalizePath(path));
    }

    private Object firstJsonPath(List<Map<String, Object>> items, String path) {
        if (items.isEmpty()) return null;
        Map<String, Object> first = items.getFirst();
        return AutomationDataSupport.readPath(first.getOrDefault("json", first), normalizePath(path));
    }

    private Object applyStringMethod(Object value, String expression) {
        if (expression.endsWith(".toUpperCase()")) return Objects.toString(value, "").toUpperCase();
        if (expression.endsWith(".toLowerCase()")) return Objects.toString(value, "").toLowerCase();
        if (expression.endsWith(".trim()")) return Objects.toString(value, "").trim();
        if (expression.endsWith(".length")) {
            if (value instanceof Map<?, ?> map) return map.size();
            if (value instanceof List<?> list) return list.size();
            return Objects.toString(value, "").length();
        }
        return value;
    }

    private String stripMethod(String expression) {
        for (String suffix : List.of(".toUpperCase()", ".toLowerCase()", ".trim()", ".length")) {
            if (expression.endsWith(suffix)) return expression.substring(0, expression.length() - suffix.length());
        }
        return expression;
    }

    private Object applyOperator(Object left, String operator, Object right) {
        return switch (operator) {
            case "??" -> left == null ? right : left;
            case "||" -> truthy(left) ? left : right;
            case "&&" -> truthy(left) ? right : left;
            case "===", "==" -> Objects.equals(normalize(left), normalize(right));
            case "!==", "!=" -> !Objects.equals(normalize(left), normalize(right));
            case ">" -> number(left) > number(right);
            case ">=" -> number(left) >= number(right);
            case "<" -> number(left) < number(right);
            case "<=" -> number(left) <= number(right);
            case "+" -> left instanceof Number && right instanceof Number
                    ? number(left) + number(right)
                    : Objects.toString(left, "") + Objects.toString(right, "");
            case "-" -> number(left) - number(right);
            case "*" -> number(left) * number(right);
            case "/" -> number(left) / number(right);
            default -> throw new IllegalArgumentException("unsupported n8n expression operator: " + operator);
        };
    }

    private int findOperator(String value, String operator) {
        int depth = 0;
        char quote = 0;
        for (int index = 0; index <= value.length() - operator.length(); index++) {
            char current = value.charAt(index);
            if (quote != 0) {
                if (current == quote && (index == 0 || value.charAt(index - 1) != '\\')) quote = 0;
                continue;
            }
            if (current == '\'' || current == '"') { quote = current; continue; }
            if (current == '(' || current == '[' || current == '{') depth++;
            if (current == ')' || current == ']' || current == '}') depth--;
            if (depth == 0 && value.startsWith(operator, index)) return index;
        }
        return -1;
    }

    private String normalizePath(String path) {
        return path.replaceAll("^\\[['\"]", "")
                .replaceAll("['\"]]$", "")
                .replaceAll("\\[['\"]([^'\"]+)['\"]]", ".$1")
                .replaceFirst("^\\.", "");
    }

    private String trimOuterParentheses(String value) {
        if (value.startsWith("(") && value.endsWith(")")) return value.substring(1, value.length() - 1).trim();
        return value;
    }

    private boolean truthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean bool) return bool;
        if (value instanceof Number number) return number.doubleValue() != 0;
        if (value instanceof String string) return !string.isBlank();
        if (value instanceof List<?> list) return !list.isEmpty();
        if (value instanceof Map<?, ?> map) return !map.isEmpty();
        return true;
    }

    private Object normalize(Object value) {
        if (value instanceof Number) return number(value);
        return value;
    }

    private double number(Object value) {
        if (value instanceof Number number) return number.doubleValue();
        try { return Double.parseDouble(Objects.toString(value, "0")); }
        catch (NumberFormatException ignored) { return 0; }
    }

    public record Evaluation(
            Map<String, Object> item,
            Map<String, Object> json,
            Map<String, Object> binary,
            List<Map<String, Object>> inputItems,
            int itemIndex,
            Map<String, Object> variables,
            Map<String, Object> execution,
            Map<String, Object> workflow,
            Map<String, Object> nodeOutputs
    ) { }
}
