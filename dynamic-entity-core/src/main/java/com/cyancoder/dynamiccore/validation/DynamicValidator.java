package com.cyancoder.dynamiccore.validation;

import com.cyancoder.dynamiccore.model.ValidationRule;

import java.util.Map;

public interface DynamicValidator {
    String name();

    ValidatorResponse validate(
            ValidationRule rule,
            String path,
            Object value,
            Map<String, Object> currentObject,
            Map<String, Object> fullInput,
            Map<String, Object> params,
            String serviceKey,
            String entityKey
    );
}
