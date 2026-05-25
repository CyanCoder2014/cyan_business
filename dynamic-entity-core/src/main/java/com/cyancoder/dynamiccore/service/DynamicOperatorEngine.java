package com.cyancoder.dynamiccore.service;

import com.cyancoder.dynamiccore.model.FieldDefinition;
import com.cyancoder.dynamiccore.model.OperationRule;
import com.cyancoder.dynamiccore.operator.DynamicOperator;
import com.cyancoder.dynamiccore.operator.OperatorRegistry;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DynamicOperatorEngine {

    private final OperatorRegistry operatorRegistry;

    public DynamicOperatorEngine(OperatorRegistry operatorRegistry) {
        this.operatorRegistry = operatorRegistry;
    }

    public Map<String, Object> apply(Map<String, FieldDefinition> fields, List<OperationRule> operations, Map<String, Object> input) {
        Map<String, Object> fullInput = input;
        if (operations != null) {
            for (OperationRule rule : operations.stream().sorted(Comparator.comparingInt(OperationRule::getOrder)).toList()) {
                DynamicOperator operator = operatorRegistry.get(rule.getOperation());
                fullInput = operator.apply(rule, "", null, fullInput, fullInput);
            }
        }
        if (fields != null) {
            for (Map.Entry<String, FieldDefinition> entry : fields.entrySet()) {
                fullInput = applyFieldOperations(entry.getKey(), entry.getValue(), fullInput);
            }
        }
        return fullInput;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> applyFieldOperations(String path, FieldDefinition definition, Map<String, Object> fullInput) {
        Object value = fullInput.get(path);
        if (definition.getOperations() != null) {
            for (OperationRule rule : definition.getOperations().stream().sorted(Comparator.comparingInt(OperationRule::getOrder)).toList()) {
                fullInput = operatorRegistry.get(rule.getOperation()).apply(rule, path, value, fullInput, fullInput);
            }
        }
        if ("object".equals(definition.getType()) && value instanceof Map<?, ?> objectValue && definition.getItemValidations() != null) {
            Map<String, Object> nested = (Map<String, Object>) objectValue;
            for (Map.Entry<String, FieldDefinition> entry : definition.getItemValidations().entrySet()) {
                Object nestedValue = nested.get(entry.getKey());
                if (entry.getValue().getOperations() != null) {
                    for (OperationRule rule : entry.getValue().getOperations().stream().sorted(Comparator.comparingInt(OperationRule::getOrder)).toList()) {
                        nested = operatorRegistry.get(rule.getOperation()).apply(rule, entry.getKey(), nestedValue, nested, nested);
                    }
                }
            }
        }
        return fullInput;
    }
}
