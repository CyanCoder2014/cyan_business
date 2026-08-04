package com.cyancoder.automationorchestrator.domain;

public record AutomationErrorPolicy(Boolean continueOnFail, Boolean deadLetterOnFailure, String fallbackNodeId) {
}
