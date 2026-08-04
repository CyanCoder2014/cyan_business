package com.cyancoder.automationorchestrator.domain;

import java.util.Map;

public record AutomationNode(
        String id,
        AutomationNodeType type,
        String name,
        Boolean enabled,
        String credentialRef,
        AutomationRetryPolicy retryPolicy,
        AutomationTimeoutPolicy timeoutPolicy,
        AutomationErrorPolicy errorPolicy,
        AutomationConcurrencyPolicy concurrencyPolicy,
        Map<String, Object> config,
        Map<String, Object> position,
        Object data
) {
    public boolean isEnabled() {
        return enabled == null || enabled;
    }

    public Map<String, Object> configOrEmpty() {
        return config == null ? Map.of() : config;
    }
}
