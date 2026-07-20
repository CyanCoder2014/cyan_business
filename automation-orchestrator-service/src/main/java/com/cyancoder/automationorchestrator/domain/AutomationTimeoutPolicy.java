package com.cyancoder.automationorchestrator.domain;

public record AutomationTimeoutPolicy(Integer connectTimeoutMs, Integer readTimeoutMs) {
}
