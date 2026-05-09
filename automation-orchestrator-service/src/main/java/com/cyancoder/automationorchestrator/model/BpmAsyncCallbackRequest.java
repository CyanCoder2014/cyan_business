package com.cyancoder.automationorchestrator.model;

import java.util.Map;

public record BpmAsyncCallbackRequest(
        String callbackId,
        String status,
        String nextState,
        Map<String, Object> payload,
        Map<String, Object> context
) {
}
