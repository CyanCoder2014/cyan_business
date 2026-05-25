package com.cyancoder.dynamiccore.validation.validators;

import com.cyancoder.dynamiccore.model.ValidationRule;
import com.cyancoder.dynamiccore.validation.DynamicValidator;
import com.cyancoder.dynamiccore.validation.ValidatorResponse;

import java.util.Map;

public class MinLengthValidator implements DynamicValidator {
    @Override
    public String name() {
        return "MIN_LENGTH";
    }

    @Override
    public ValidatorResponse validate(ValidationRule rule, String path, Object value, Map<String, Object> currentObject, Map<String, Object> fullInput, Map<String, Object> params, String serviceKey, String entityKey) {
        if (value == null) {
            return new ValidatorResponse(true, fullInput, null);
        }
        int min = Integer.parseInt(String.valueOf(params.getOrDefault("min", 0)));
        boolean valid = String.valueOf(value).length() >= min;
        return new ValidatorResponse(valid, fullInput, valid ? null : rule.getValidationMessage());
    }
}
