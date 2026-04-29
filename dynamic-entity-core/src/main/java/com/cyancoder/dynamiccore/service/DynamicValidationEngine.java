package com.cyancoder.dynamiccore.service;

import com.cyancoder.dynamiccore.model.DynamicValidationError;
import com.cyancoder.dynamiccore.model.DynamicValidationResult;
import com.cyancoder.dynamiccore.model.FieldDefinition;
import com.cyancoder.dynamiccore.model.ValidationRule;
import com.cyancoder.dynamiccore.validation.DynamicValidator;
import com.cyancoder.dynamiccore.validation.ValidatorRegistry;
import com.cyancoder.dynamiccore.validation.ValidatorResponse;

import java.util.*;

public class DynamicValidationEngine {

    private final ValidatorRegistry validatorRegistry;
    private final boolean checkMissingFields;
    private final boolean checkExtraFields;

    public DynamicValidationEngine(ValidatorRegistry validatorRegistry, boolean checkMissingFields, boolean checkExtraFields) {
        this.validatorRegistry = validatorRegistry;
        this.checkMissingFields = checkMissingFields;
        this.checkExtraFields = checkExtraFields;
    }

    public DynamicValidationResult validate(
            String serviceKey,
            String entityKey,
            Map<String, FieldDefinition> fields,
            List<ValidationRule> validations,
            Map<String, Object> input,
            boolean firstInput
    ) {
        Map<String, Object> fullInput = new LinkedHashMap<>(input == null ? Map.of() : input);
        List<DynamicValidationError> errors = new ArrayList<>();

        if (validations != null) {
            for (ValidationRule rule : validations.stream().sorted(Comparator.comparingInt(ValidationRule::getOrder)).toList()) {
                ValidatorResponse response = validatorRegistry.get(rule.getValidation())
                        .validate(rule, "entity", null, fullInput, fullInput, safeParams(rule), serviceKey, entityKey);
                fullInput = response.fullInput();
                if (!response.valid()) {
                    errors.add(new DynamicValidationError("entity", message(rule, response.validationMessage())));
                }
            }
        }

        validateFields("", fields == null ? Map.of() : fields, fullInput, fullInput, errors, serviceKey, entityKey, firstInput);
        return new DynamicValidationResult(fullInput, errors);
    }

    @SuppressWarnings("unchecked")
    private void validateFields(
            String path,
            Map<String, FieldDefinition> fields,
            Map<String, Object> data,
            Map<String, Object> fullInput,
            List<DynamicValidationError> errors,
            String serviceKey,
            String entityKey,
            boolean firstInput
    ) {
        if (data == null) {
            return;
        }

        if (checkExtraFields) {
            for (String inputField : data.keySet()) {
                if (!fields.containsKey(inputField)) {
                    errors.add(new DynamicValidationError(path.isBlank() ? inputField : path + "." + inputField, "unexpected field"));
                }
            }
            if (!errors.isEmpty()) {
                return;
            }
        }

        for (Map.Entry<String, FieldDefinition> entry : fields.entrySet()) {
            String fieldName = entry.getKey();
            FieldDefinition definition = entry.getValue();
            String currentPath = path.isBlank() ? fieldName : path + "." + fieldName;
            Object value = data.get(fieldName);

            if (!firstInput && checkMissingFields && !data.containsKey(fieldName)) {
                errors.add(new DynamicValidationError(currentPath, "field is missing"));
                continue;
            }

            List<ValidationRule> rules = definition.getValidations() == null ? List.of() : definition.getValidations().stream().sorted(Comparator.comparingInt(ValidationRule::getOrder)).toList();
            for (ValidationRule rule : rules) {
                DynamicValidator validator = validatorRegistry.get(rule.getValidation());
                ValidatorResponse response = validator.validate(rule, currentPath, value, data, fullInput, safeParams(rule), serviceKey, entityKey);
                if (!response.valid()) {
                    errors.add(new DynamicValidationError(currentPath, message(rule, response.validationMessage())));
                }
                Map<String, Object> updatedInput = new LinkedHashMap<>(response.fullInput() == null ? Map.of() : response.fullInput());
                fullInput.clear();
                fullInput.putAll(updatedInput);
                value = data.get(fieldName);
            }

            if ("object".equals(definition.getType())) {
                if (value instanceof Map<?, ?> nestedObject) {
                    validateFields(currentPath, definition.getItemValidations() == null ? Map.of() : definition.getItemValidations(), (Map<String, Object>) nestedObject, fullInput, errors, serviceKey, entityKey, firstInput);
                } else if (value != null) {
                    errors.add(new DynamicValidationError(currentPath, "expected object"));
                }
            } else if ("list".equals(definition.getType())) {
                if (value instanceof List<?> list) {
                    for (int i = 0; i < list.size(); i++) {
                        Object item = list.get(i);
                        if (item instanceof Map<?, ?> nestedItem) {
                            validateFields(currentPath + "[" + i + "]", definition.getItemValidations() == null ? Map.of() : definition.getItemValidations(), (Map<String, Object>) nestedItem, fullInput, errors, serviceKey, entityKey, firstInput);
                        } else if (item != null && definition.getItemValidations() != null && !definition.getItemValidations().isEmpty()) {
                            errors.add(new DynamicValidationError(currentPath + "[" + i + "]", "expected object"));
                        }
                    }
                } else if (value != null) {
                    errors.add(new DynamicValidationError(currentPath, "expected list"));
                }
            }
        }
    }

    private Map<String, Object> safeParams(ValidationRule rule) {
        return rule.getValidationParams() == null ? Map.of() : rule.getValidationParams();
    }

    private String message(ValidationRule rule, String responseMessage) {
        return responseMessage == null || responseMessage.isBlank() ? rule.getValidationMessage() : responseMessage;
    }
}
