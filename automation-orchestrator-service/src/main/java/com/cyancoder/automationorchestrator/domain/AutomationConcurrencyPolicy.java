package com.cyancoder.automationorchestrator.domain;

public record AutomationConcurrencyPolicy(String keyExpression, Integer maxConcurrency) {
}
