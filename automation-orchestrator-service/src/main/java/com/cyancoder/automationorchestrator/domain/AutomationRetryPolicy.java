package com.cyancoder.automationorchestrator.domain;

public record AutomationRetryPolicy(Integer maxAttempts, Long backoffMs, String strategy) {
}
