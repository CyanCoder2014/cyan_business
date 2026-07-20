package com.cyancoder.automationorchestrator.domain;

public record AutomationEdge(String id, String fromNodeId, String fromPort, String toNodeId) {
}
