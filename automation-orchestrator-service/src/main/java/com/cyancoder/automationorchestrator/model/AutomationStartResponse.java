package com.cyancoder.automationorchestrator.model;

import java.util.Map;

public record AutomationStartResponse(
        String executionId,
        String automationFlowKey,
        String correlationKey,
        String status,
        Map<String, Object> snapshot
) {
}
