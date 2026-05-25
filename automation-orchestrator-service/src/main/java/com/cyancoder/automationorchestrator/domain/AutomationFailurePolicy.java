package com.cyancoder.automationorchestrator.domain;

public enum AutomationFailurePolicy {
    FAIL_FAST,
    MARK_FAILED,
    CONTINUE,
    RETRY
}
