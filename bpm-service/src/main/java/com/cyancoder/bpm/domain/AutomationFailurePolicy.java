package com.cyancoder.bpm.domain;

public enum AutomationFailurePolicy {
    FAIL_FAST,
    MARK_FAILED,
    CONTINUE,
    RETRY
}
