package com.cyancoder.automationorchestrator.model;

import java.util.Map;

public record AutomationNodeCallbackRequest(String callbackId, Map<String, Object> payload) {
}
