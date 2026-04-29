package com.cyancoder.dynamiccore.validation.validators;

import com.cyancoder.dynamiccore.model.ValidationRule;
import com.cyancoder.dynamiccore.validation.DynamicValidator;
import com.cyancoder.dynamiccore.validation.ValidatorResponse;

import java.util.Map;

public class MaxLengthValidator implements DynamicValidator {
    @Override
    public String name() {
        return "MAX_LENGTH";
    }

    @Override
    public ValidatorResponse validate(ValidationRule rule, String path, Object value, Map<String, Object> currentObject, Map<String, Object> fullInput, Map<String, Object> params, String serviceKey, String entityKey) {
        if (value == null) {
            return new ValidatorResponse(true, fullInput, null);
        }
        int max = Integer.parseInt(String.valueOf(params.getOrDefault("max", Integer.MAX_VALUE)));
        boolean valid = String.valueOf(value).length() <= max;
        return new ValidatorResponse(valid, fullInput, valid ? null : rule.getValidationMessage());
    }
}
