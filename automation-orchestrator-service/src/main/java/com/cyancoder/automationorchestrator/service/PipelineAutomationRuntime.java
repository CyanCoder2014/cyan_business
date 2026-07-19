package com.cyancoder.automationorchestrator.service;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class PipelineAutomationRuntime {
    private static final Pattern TEMPLATE = Pattern.compile("\\{\\{\\s*([^}]+?)\\s*}}");

    private final InternalServiceHttpSupport httpSupport;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    PipelineAutomationRuntime(InternalServiceHttpSupport httpSupport) {
        this.httpSupport = httpSupport;
    }

    Map<String, Object> execute(Map<String, Object> fragment, Map<String, Object> input, String tenantKey, String siteKey) {
        Map<String, Object> variables = deepMap(input);
        executeSteps(asList(fragment.get("steps")), variables, new LinkedHashMap<>(), tenantKey, siteKey);
        String outputPath = string(fragment.get("outputPath"));
        if (outputPath == null) {
            return variables;
        }
        Object output = readPath(variables, outputPath);
        if (output instanceof Map<?, ?> map) {
            return deepMap(map);
        }
        return Map.of("result", output == null ? Map.of() : output);
    }

    private void executeSteps(List<Object> steps, Map<String, Object> variables, Map<String, Object> context,
                              String tenantKey, String siteKey) {
        for (Object value : steps) {
            Map<String, Object> step = asMap(value);
            String type = string(step.get("type"));
            if (type == null) {
                throw new IllegalArgumentException("automation step type is required");
            }
            switch (type.toUpperCase()) {
                case "MAP_FIELDS" -> mapFields(step, variables, context);
                case "FOR_EACH" -> forEach(step, variables, context, tenantKey, siteKey);
                case "SCRIPT", "CODE" -> runScript(step, variables, context);
                case "CALL_API", "SAVE" -> callApi(step, variables, context, tenantKey, siteKey);
                default -> throw new IllegalArgumentException("unsupported automation step type: " + type);
            }
        }
    }

    private void mapFields(Map<String, Object> step, Map<String, Object> variables, Map<String, Object> context) {
        for (Map.Entry<String, Object> entry : asMap(step.get("mappings")).entrySet()) {
            Object value = materialize(entry.getValue(), variables, context);
            if (value instanceof String path && isPath(path)) {
                Object resolved = resolvePath(path, variables, context);
                value = resolved == null ? value : resolved;
            }
            setPath(variables, entry.getKey(), value);
        }
    }

    private void forEach(Map<String, Object> step, Map<String, Object> variables, Map<String, Object> context,
                         String tenantKey, String siteKey) {
        Object source = resolvePath(string(step.get("sourcePath")), variables, context);
        List<Object> items = asList(source);
        List<Object> results = new ArrayList<>();
        List<Object> nestedSteps = asList(step.get("steps"));
        for (int index = 0; index < items.size(); index++) {
            Map<String, Object> itemContext = new LinkedHashMap<>(context);
            itemContext.put(stringOrDefault(step.get("itemVariable"), "item"), items.get(index));
            itemContext.put("item", items.get(index));
            itemContext.put("index", index);
            if (!nestedSteps.isEmpty()) {
                Map<String, Object> localVariables = deepMap(variables);
                executeSteps(nestedSteps, localVariables, itemContext, tenantKey, siteKey);
                String resultPath = string(step.get("resultPath"));
                results.add(resultPath == null ? localVariables : readPath(localVariables, resultPath));
            } else {
                Object template = step.containsKey("itemTemplate") ? step.get("itemTemplate") : items.get(index);
                results.add(materialize(template, variables, itemContext));
            }
        }
        setPath(variables, stringOrDefault(step.get("targetPath"), "forEachResult"), results);
    }

    private void runScript(Map<String, Object> step, Map<String, Object> variables, Map<String, Object> context) {
        String expression = string(step.get("expression"));
        if (expression == null) {
            throw new IllegalArgumentException("SCRIPT expression is required");
        }
        SimpleEvaluationContext evaluationContext = SimpleEvaluationContext.forReadOnlyDataBinding()
                .withInstanceMethods()
                .build();
        evaluationContext.setVariable("variables", variables);
        evaluationContext.setVariable("context", context);
        evaluationContext.setVariable("item", context.get("item"));
        evaluationContext.setVariable("index", context.get("index"));
        Object result = expressionParser.parseExpression(expression).getValue(evaluationContext);
        setPath(variables, stringOrDefault(step.get("targetPath"), "scriptResult"), result);
    }

    @SuppressWarnings("unchecked")
    private void callApi(Map<String, Object> step, Map<String, Object> variables, Map<String, Object> context,
                         String tenantKey, String siteKey) {
        String serviceKey = string(materialize(step.get("serviceKey"), variables, context));
        String path = string(materialize(step.get("path"), variables, context));
        if (serviceKey == null || path == null || !path.startsWith("/")) {
            throw new IllegalArgumentException("CALL_API requires serviceKey and an absolute service path");
        }
        HttpMethod method = HttpMethod.valueOf(stringOrDefault(materialize(step.get("method"), variables, context), "POST").toUpperCase());
        Object body = materialize(step.get("body"), variables, context);
        HttpHeaders headers = httpSupport.internalHeaders(serviceKey, tenantKey, siteKey);
        asMap(materialize(step.get("headers"), variables, context)).forEach((key, value) -> headers.set(key, string(value)));
        Map<String, Object> response = httpSupport.exchange(serviceKey, path, method, body, headers, Map.class);
        Map<String, Object> safeResponse = response == null ? Map.of() : response;
        String storeResponseAt = string(step.get("storeResponseAt"));
        if (storeResponseAt != null) {
            setPath(variables, storeResponseAt, safeResponse);
        }
        for (Map.Entry<String, Object> mapping : asMap(step.get("responseMappings")).entrySet()) {
            setPath(variables, mapping.getKey(), readPath(safeResponse, string(mapping.getValue())));
        }
    }

    private Object materialize(Object value, Map<String, Object> variables, Map<String, Object> context) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((key, child) -> {
                if (key != null) result.put(key.toString(), materialize(child, variables, context));
            });
            return result;
        }
        if (value instanceof Iterable<?> iterable) {
            List<Object> result = new ArrayList<>();
            iterable.forEach(child -> result.add(materialize(child, variables, context)));
            return result;
        }
        if (!(value instanceof String template)) {
            return value;
        }
        Matcher matcher = TEMPLATE.matcher(template);
        if (matcher.matches()) {
            return resolvePath(matcher.group(1).trim(), variables, context);
        }
        StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            Object resolved = resolvePath(matcher.group(1).trim(), variables, context);
            matcher.appendReplacement(output, Matcher.quoteReplacement(resolved == null ? "" : resolved.toString()));
        }
        matcher.appendTail(output);
        return output.toString();
    }

    private Object resolvePath(String path, Map<String, Object> variables, Map<String, Object> context) {
        if (path == null) return null;
        Object fromContext = readPath(context, path);
        return fromContext == null ? readPath(variables, path) : fromContext;
    }

    @SuppressWarnings("unchecked")
    private Object readPath(Object source, String path) {
        if (source == null || path == null || path.isBlank()) return source;
        Object current = source;
        for (String segment : path.replace("$.", "").split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) return null;
            current = map.get(segment);
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    private void setPath(Map<String, Object> target, String path, Object value) {
        if (path == null || path.isBlank()) throw new IllegalArgumentException("target path is required");
        String[] segments = path.replace("$.", "").split("\\.");
        Map<String, Object> current = target;
        for (int index = 0; index < segments.length - 1; index++) {
            Object child = current.get(segments[index]);
            if (!(child instanceof Map<?, ?>)) {
                child = new LinkedHashMap<String, Object>();
                current.put(segments[index], child);
            }
            current = (Map<String, Object>) child;
        }
        current.put(segments[segments.length - 1], value);
    }

    private boolean isPath(String value) {
        return value.matches("[A-Za-z0-9_$.-]+");
    }

    private String string(Object value) { return value == null ? null : value.toString(); }
    private String stringOrDefault(Object value, String fallback) { String result = string(value); return result == null || result.isBlank() ? fallback : result; }
    private Map<String, Object> asMap(Object value) { return value instanceof Map<?, ?> map ? deepMap(map) : Map.of(); }
    private List<Object> asList(Object value) {
        if (value instanceof List<?> list) return new ArrayList<>(list);
        if (value instanceof Iterable<?> iterable) { List<Object> result = new ArrayList<>(); iterable.forEach(result::add); return result; }
        return List.of();
    }
    private Map<String, Object> deepMap(Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (value instanceof Map<?, ?> map) map.forEach((key, child) -> { if (key != null) result.put(key.toString(), deepCopy(child)); });
        return result;
    }
    private Object deepCopy(Object value) {
        if (value instanceof Map<?, ?>) return deepMap(value);
        if (value instanceof List<?> list) return list.stream().map(this::deepCopy).toList();
        return value;
    }
}
