package com.cyancoder.processor.service;

import com.cyancoder.processor.entity.ProcessorDefinition;
import com.cyancoder.processor.model.ProcessorRule;
import com.cyancoder.processor.model.ProcessorRunRequest;
import com.cyancoder.processor.model.ProcessorRunResponse;
import com.cyancoder.processor.repository.ProcessorDefinitionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ProcessorExecutionService {

    private final ProcessorDefinitionRepository repository;
    private final ObjectMapper objectMapper;

    public ProcessorExecutionService(ProcessorDefinitionRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public ProcessorRunResponse run(String processorKey, ProcessorRunRequest request) {
        ProcessorDefinition definition = repository.findByProcessorKey(processorKey).orElseThrow();
        if (!definition.isActive()) {
            return new ProcessorRunResponse(false, List.of("processor is inactive"), safePayload(request));
        }
        if (definition.getTargetType() != null && request.targetType() != null
                && !definition.getTargetType().equalsIgnoreCase(request.targetType())) {
            return new ProcessorRunResponse(false, List.of("target type mismatch"), safePayload(request));
        }

        Map<String, Object> payload = new LinkedHashMap<>(safePayload(request));
        List<ProcessorRule> operatorRules = parseRules(definition.getOperatorsJson());
        for (ProcessorRule rule : operatorRules) {
            applyOperator(payload, rule);
        }

        List<String> errors = new ArrayList<>();
        List<ProcessorRule> validatorRules = parseRules(definition.getValidatorsJson());
        for (ProcessorRule rule : validatorRules) {
            validate(payload, rule, errors);
        }

        return new ProcessorRunResponse(errors.isEmpty(), errors, payload);
    }

    private Map<String, Object> safePayload(ProcessorRunRequest request) {
        return request.payload() == null ? Map.of() : request.payload();
    }

    private List<ProcessorRule> parseRules(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid processor rule json", ex);
        }
    }

    private void applyOperator(Map<String, Object> payload, ProcessorRule rule) {
        String type = safe(rule.type());
        if ("SET_FIELD".equals(type)) {
            payload.put(targetField(rule), rule.value());
            return;
        }
        if ("COPY_FIELD".equals(type)) {
            payload.put(targetField(rule), payload.get(rule.sourceField()));
            return;
        }
        if ("TRIM".equals(type)) {
            Object value = payload.get(rule.field());
            if (value != null) {
                payload.put(rule.field(), value.toString().trim());
            }
            return;
        }
        if ("UPPERCASE".equals(type)) {
            Object value = payload.get(rule.field());
            if (value != null) {
                payload.put(rule.field(), value.toString().toUpperCase());
            }
            return;
        }
        if ("LOWERCASE".equals(type)) {
            Object value = payload.get(rule.field());
            if (value != null) {
                payload.put(rule.field(), value.toString().toLowerCase());
            }
            return;
        }
        if ("CONCAT_FIELDS".equals(type)) {
            List<String> fields = rule.sourceFields() == null ? List.of() : rule.sourceFields();
            String separator = rule.separator() == null ? "" : rule.separator();
            String joined = fields.stream()
                    .map(payload::get)
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .reduce((left, right) -> left + separator + right)
                    .orElse("");
            payload.put(targetField(rule), joined);
            return;
        }
        if ("SUM_FIELDS".equals(type)) {
            BigDecimal total = BigDecimal.ZERO;
            for (String field : rule.sourceFields() == null ? List.<String>of() : rule.sourceFields()) {
                BigDecimal value = toDecimal(payload.get(field));
                if (value != null) {
                    total = total.add(value);
                }
            }
            payload.put(targetField(rule), total);
            return;
        }
        if ("MULTIPLY_FIELDS".equals(type)) {
            BigDecimal result = null;
            for (String field : rule.sourceFields() == null ? List.<String>of() : rule.sourceFields()) {
                BigDecimal value = toDecimal(payload.get(field));
                if (value != null) {
                    result = result == null ? value : result.multiply(value);
                }
            }
            if (result != null) {
                payload.put(targetField(rule), result);
            }
        }
    }

    private void validate(Map<String, Object> payload, ProcessorRule rule, List<String> errors) {
        String type = safe(rule.type());
        Object raw = payload.get(rule.field());
        if ("REQUIRED".equals(type) && isBlank(raw)) {
            errors.add(message(rule, rule.field() + " is required"));
            return;
        }
        if ("MIN_LENGTH".equals(type) && raw != null && raw.toString().length() < parseInt(rule.value(), 0)) {
            errors.add(message(rule, rule.field() + " is shorter than allowed"));
            return;
        }
        if ("MAX_LENGTH".equals(type) && raw != null && raw.toString().length() > parseInt(rule.value(), Integer.MAX_VALUE)) {
            errors.add(message(rule, rule.field() + " is longer than allowed"));
            return;
        }
        if ("REGEX".equals(type) && raw != null && rule.pattern() != null && !Pattern.compile(rule.pattern()).matcher(raw.toString()).matches()) {
            errors.add(message(rule, rule.field() + " has invalid format"));
            return;
        }
        if ("ENUM".equals(type) && raw != null && rule.values() != null && !rule.values().contains(raw.toString())) {
            errors.add(message(rule, rule.field() + " is not in allowed values"));
            return;
        }
        if ("DECIMAL_MIN".equals(type)) {
            BigDecimal left = toDecimal(raw);
            BigDecimal right = toDecimal(rule.value());
            if (left == null || right == null || left.compareTo(right) < 0) {
                errors.add(message(rule, rule.field() + " is below minimum"));
            }
            return;
        }
        if ("DECIMAL_MAX".equals(type)) {
            BigDecimal left = toDecimal(raw);
            BigDecimal right = toDecimal(rule.value());
            if (left == null || right == null || left.compareTo(right) > 0) {
                errors.add(message(rule, rule.field() + " is above maximum"));
            }
        }
    }

    private String targetField(ProcessorRule rule) {
        return rule.targetField() == null || rule.targetField().isBlank() ? rule.field() : rule.targetField();
    }

    private String message(ProcessorRule rule, String fallback) {
        return rule.message() == null || rule.message().isBlank() ? fallback : rule.message();
    }

    private String safe(String value) {
        return value == null ? "" : value.toUpperCase(Locale.ROOT);
    }

    private boolean isBlank(Object value) {
        return value == null || value.toString().isBlank();
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private BigDecimal toDecimal(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (Exception ex) {
            return null;
        }
    }
}
