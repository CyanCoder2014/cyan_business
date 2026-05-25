package com.cyancoder.dynamiccore.validation.validators;

import com.cyancoder.dynamiccore.model.ValidationRule;
import com.cyancoder.dynamiccore.validation.DynamicValidator;
import com.cyancoder.dynamiccore.validation.ValidatorResponse;

import java.util.Map;

public class RequiredValidator implements DynamicValidator {
    @Override
    public String name() {
        return "REQUIRED";
    }

    @Override
    public ValidatorResponse validate(ValidationRule rule, String path, Object value, Map<String, Object> currentObject, Map<String, Object> fullInput, Map<String, Object> params, String serviceKey, String entityKey) {
        boolean valid = !(value == null || (value instanceof String text && text.isBlank()));
        return new ValidatorResponse(valid, fullInput, valid ? null : rule.getValidationMessage());
    }
}
