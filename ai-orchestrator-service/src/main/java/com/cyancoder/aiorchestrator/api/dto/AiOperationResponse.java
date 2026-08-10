package com.cyancoder.aiorchestrator.api.dto;

import java.time.Instant;

public record AiOperationResponse(
        String status,
        AiOperationRequest.AiOperationType operation,
        Object output,
        Instant completedAt,
        String providerProfileKey,
        java.util.Map<String,Object> usage
) {
    public AiOperationResponse(String status,AiOperationRequest.AiOperationType operation,Object output,Instant completedAt){this(status,operation,output,completedAt,null,java.util.Map.of());}
}
