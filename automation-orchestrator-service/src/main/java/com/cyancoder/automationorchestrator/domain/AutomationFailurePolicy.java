package com.cyancoder.automationorchestrator.domain;

public enum AutomationFailurePolicy {
    FAIL_FAST,
    MARK_FAILED,
    CONTINUE,
    RETRY;

    @com.fasterxml.jackson.annotation.JsonCreator
    public static AutomationFailurePolicy from(String value) {
        if (value == null || value.isBlank()) return FAIL_FAST;
        if ("FAIL".equalsIgnoreCase(value)) return FAIL_FAST;
        return valueOf(value.trim().toUpperCase());
    }
}
