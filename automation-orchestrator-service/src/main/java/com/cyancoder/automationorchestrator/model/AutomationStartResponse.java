package com.cyancoder.automationorchestrator.model;

import java.util.Map;

public record AutomationStartResponse(
        String executionId,
        String blockKey,
        String automationFlowKey,
        String correlationKey,
        String status,
        Map<String, Object> snapshot,
        Map<String, Object> output,
        Map<String, Object> error
) {
}
