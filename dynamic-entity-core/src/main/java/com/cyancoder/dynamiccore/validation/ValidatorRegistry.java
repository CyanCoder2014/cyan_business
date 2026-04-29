package com.cyancoder.dynamiccore.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidatorRegistry {

    private final Map<String, DynamicValidator> validators = new HashMap<>();

    public ValidatorRegistry(List<DynamicValidator> validatorList) {
        for (DynamicValidator validator : validatorList) {
            validators.put(validator.name().toUpperCase(), validator);
        }
    }

    public DynamicValidator get(String name) {
        DynamicValidator validator = validators.get(name == null ? "" : name.toUpperCase());
        if (validator == null) {
            throw new IllegalArgumentException("validator not found: " + name);
        }
        return validator;
    }
}
