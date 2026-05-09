package com.cyancoder.automationorchestrator.model;

import java.util.Map;

public record AutomationStartRequest(
        String automationFlowKey,
        String correlationKey,
        String callbackPath,
        String tenantKey,
        String siteKey,
        Map<String, Object> input,
        Map<String, Object> context
) {
}
