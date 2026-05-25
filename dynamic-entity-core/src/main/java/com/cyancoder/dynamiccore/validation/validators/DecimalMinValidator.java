package com.cyancoder.dynamiccore.validation.validators;

import com.cyancoder.dynamiccore.model.ValidationRule;
import com.cyancoder.dynamiccore.validation.DynamicValidator;
import com.cyancoder.dynamiccore.validation.ValidatorResponse;

import java.math.BigDecimal;
import java.util.Map;

public class DecimalMinValidator implements DynamicValidator {
    @Override
    public String name() {
        return "DECIMAL_MIN";
    }

    @Override
    public ValidatorResponse validate(ValidationRule rule, String path, Object value, Map<String, Object> currentObject, Map<String, Object> fullInput, Map<String, Object> params, String serviceKey, String entityKey) {
        if (value == null) {
            return new ValidatorResponse(true, fullInput, null);
        }
        BigDecimal left = new BigDecimal(String.valueOf(value));
        BigDecimal right = new BigDecimal(String.valueOf(params.getOrDefault("min", "0")));
        boolean valid = left.compareTo(right) >= 0;
        return new ValidatorResponse(valid, fullInput, valid ? null : rule.getValidationMessage());
    }
}
