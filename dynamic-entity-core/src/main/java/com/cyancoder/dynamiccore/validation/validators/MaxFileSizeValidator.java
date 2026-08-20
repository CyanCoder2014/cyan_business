package com.cyancoder.dynamiccore.validation.validators;

import com.cyancoder.dynamiccore.model.ValidationRule;
import com.cyancoder.dynamiccore.validation.DynamicValidator;
import com.cyancoder.dynamiccore.validation.ValidatorResponse;

import java.util.List;
import java.util.Map;

public class MaxFileSizeValidator implements DynamicValidator {
    @Override
    public String name() {
        return "MAX_FILE_SIZE";
    }

    @Override
    public ValidatorResponse validate(ValidationRule rule, String path, Object value, Map<String, Object> currentObject, Map<String, Object> fullInput, Map<String, Object> params, String serviceKey, String entityKey) {
        if (value == null) {
            return new ValidatorResponse(true, fullInput, null);
        }
        long maxBytes = Long.parseLong(String.valueOf(params.getOrDefault("maxBytes", Long.MAX_VALUE)));
        List<?> files = value instanceof List<?> list ? list : List.of(value);
        for (Object file : files) {
            if (!(file instanceof Map<?, ?> map)) continue;
            Object sizeRaw = map.get("sizeBytes");
            long size = sizeRaw == null ? 0 : Long.parseLong(String.valueOf(sizeRaw));
            if (size > maxBytes) {
                return new ValidatorResponse(false, fullInput, rule.getValidationMessage());
            }
        }
        return new ValidatorResponse(true, fullInput, null);
    }
}
