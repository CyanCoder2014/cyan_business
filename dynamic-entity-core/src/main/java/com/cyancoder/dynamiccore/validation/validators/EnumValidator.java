package com.cyancoder.dynamiccore.validation.validators;

import com.cyancoder.dynamiccore.model.ValidationRule;
import com.cyancoder.dynamiccore.validation.DynamicValidator;
import com.cyancoder.dynamiccore.validation.ValidatorResponse;

import java.util.Collection;
import java.util.Map;

public class EnumValidator implements DynamicValidator {
    @Override
    public String name() {
        return "ENUM";
    }

    @Override
    public ValidatorResponse validate(ValidationRule rule, String path, Object value, Map<String, Object> currentObject, Map<String, Object> fullInput, Map<String, Object> params, String serviceKey, String entityKey) {
        if (value == null) {
            return new ValidatorResponse(true, fullInput, null);
        }
        Object raw = params.get("values");
        boolean valid = raw instanceof Collection<?> collection && collection.stream().anyMatch(item -> String.valueOf(item).equals(String.valueOf(value)));
        return new ValidatorResponse(valid, fullInput, valid ? null : rule.getValidationMessage());
    }
}
