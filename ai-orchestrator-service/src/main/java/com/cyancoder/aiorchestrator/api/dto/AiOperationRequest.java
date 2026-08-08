package com.cyancoder.aiorchestrator.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record AiOperationRequest(
        @NotNull AiOperationType operation,
        @NotBlank String instructions,
        Object input,
        Map<String, Object> outputSchema,
        String locale
) {
    public enum AiOperationType { TRANSFORM_DATA, GENERATE_CONTENT, GENERATE_DSL }
}
