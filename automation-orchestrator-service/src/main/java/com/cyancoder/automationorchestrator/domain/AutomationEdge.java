package com.cyancoder.automationorchestrator.domain;

public record AutomationEdge(String id, String fromNodeId, String fromPort, String toNodeId, String toPort) {
    public AutomationEdge(String id, String fromNodeId, String fromPort, String toNodeId) {
        this(id, fromNodeId, fromPort, toNodeId, null);
    }
}
