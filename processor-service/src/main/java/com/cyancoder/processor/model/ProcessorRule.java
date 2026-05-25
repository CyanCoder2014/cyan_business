package com.cyancoder.processor.model;

import java.util.List;

public record ProcessorRule(
        String type,
        String field,
        String targetField,
        String sourceField,
        List<String> sourceFields,
        String message,
        String value,
        List<String> values,
        String pattern,
        String separator
) {
}
