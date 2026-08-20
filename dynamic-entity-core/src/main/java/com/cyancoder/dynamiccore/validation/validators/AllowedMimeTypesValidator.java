package com.cyancoder.dynamiccore.validation.validators;

import com.cyancoder.dynamiccore.model.ValidationRule;
import com.cyancoder.dynamiccore.validation.DynamicValidator;
import com.cyancoder.dynamiccore.validation.ValidatorResponse;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AllowedMimeTypesValidator implements DynamicValidator {
    @Override
    public String name() {
        return "ALLOWED_MIME_TYPES";
    }

    @Override
    public ValidatorResponse validate(ValidationRule rule, String path, Object value, Map<String, Object> currentObject, Map<String, Object> fullInput, Map<String, Object> params, String serviceKey, String entityKey) {
        if (value == null) {
            return new ValidatorResponse(true, fullInput, null);
        }
        Object typesRaw = params.get("types");
        if (!(typesRaw instanceof List<?> allowed) || allowed.isEmpty()) {
            return new ValidatorResponse(true, fullInput, null);
        }
        List<?> files = value instanceof List<?> list ? list : List.of(value);
        for (Object file : files) {
            if (!(file instanceof Map<?, ?> map)) continue;
            String mimeType = String.valueOf(map.get("mimeType")).toLowerCase(Locale.ROOT);
            boolean matches = allowed.stream().anyMatch(pattern -> matches(mimeType, String.valueOf(pattern).toLowerCase(Locale.ROOT)));
            if (!matches) {
                return new ValidatorResponse(false, fullInput, rule.getValidationMessage());
            }
        }
        return new ValidatorResponse(true, fullInput, null);
    }

    private boolean matches(String mimeType, String pattern) {
        if (pattern.endsWith("/*")) {
            return mimeType.startsWith(pattern.substring(0, pattern.length() - 1));
        }
        return mimeType.equals(pattern);
    }
}
