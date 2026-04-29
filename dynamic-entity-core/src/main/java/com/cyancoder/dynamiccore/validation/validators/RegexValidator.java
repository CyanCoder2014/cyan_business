package com.cyancoder.dynamiccore.validation.validators;

import com.cyancoder.dynamiccore.model.ValidationRule;
import com.cyancoder.dynamiccore.validation.DynamicValidator;
import com.cyancoder.dynamiccore.validation.ValidatorResponse;

import java.util.Map;
import java.util.regex.Pattern;

public class RegexValidator implements DynamicValidator {
    @Override
    public String name() {
        return "REGEX";
    }

    @Override
    public ValidatorResponse validate(ValidationRule rule, String path, Object value, Map<String, Object> currentObject, Map<String, Object> fullInput, Map<String, Object> params, String serviceKey, String entityKey) {
        if (value == null) {
            return new ValidatorResponse(true, fullInput, null);
        }
        String pattern = String.valueOf(params.getOrDefault("pattern", ".*"));
        boolean valid = Pattern.compile(pattern).matcher(String.valueOf(value)).matches();
        return new ValidatorResponse(valid, fullInput, valid ? null : rule.getValidationMessage());
    }
}
