package com.cyancoder.aiorchestrator.api.dto;

import java.time.Instant;

public record AiOperationResponse(
        String status,
        AiOperationRequest.AiOperationType operation,
        Object output,
        Instant completedAt
) {
}
