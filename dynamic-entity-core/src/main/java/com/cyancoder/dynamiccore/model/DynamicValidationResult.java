package com.cyancoder.dynamiccore.model;

import java.util.List;
import java.util.Map;

public record DynamicValidationResult(Map<String, Object> data, List<DynamicValidationError> errors) {
    public boolean valid() {
        return errors == null || errors.isEmpty();
    }
}
