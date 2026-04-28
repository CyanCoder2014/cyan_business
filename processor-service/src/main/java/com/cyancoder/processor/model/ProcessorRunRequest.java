package com.cyancoder.processor.model;

import java.util.Map;

public record ProcessorRunRequest(
        String targetType,
        Map<String, Object> payload
) {
}
