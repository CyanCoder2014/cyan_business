package com.cyancoder.automationorchestrator.domain;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class AutomationExecutionStep {
    private String nodeId;
    private String nodeType;
    private String status;
    private int attempt;
    private Map<String, Object> inputSnapshot = new LinkedHashMap<>();
    private Map<String, Object> outputSnapshot = new LinkedHashMap<>();
    private Map<String, Object> errorSnapshot = new LinkedHashMap<>();
    private Instant startedAt;
    private Instant finishedAt;
    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }
    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getAttempt() { return attempt; }
    public void setAttempt(int attempt) { this.attempt = attempt; }
    public Map<String, Object> getInputSnapshot() { return inputSnapshot; }
    public void setInputSnapshot(Map<String, Object> value) { inputSnapshot = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value); }
    public Map<String, Object> getOutputSnapshot() { return outputSnapshot; }
    public void setOutputSnapshot(Map<String, Object> value) { outputSnapshot = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value); }
    public Map<String, Object> getErrorSnapshot() { return errorSnapshot; }
    public void setErrorSnapshot(Map<String, Object> value) { errorSnapshot = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value); }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }
}
