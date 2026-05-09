package com.cyancoder.automationorchestrator.model;

import com.cyancoder.automationorchestrator.domain.AutomationExecutionMode;
import com.cyancoder.automationorchestrator.domain.AutomationFailurePolicy;

import java.util.Map;

public record AutomationStartRequest(
        String blockKey,
        String automationFlowKey,
        AutomationExecutionMode executionMode,
        AutomationFailurePolicy failurePolicy,
        String correlationKey,
        String callbackPath,
        String tenantKey,
        String siteKey,
        Map<String, Object> input,
        Map<String, Object> context,
        Map<String, Object> inlineFragment,
        Integer maxRetries,
        Long timeoutSeconds,
        Long delayMillis
) {
}
