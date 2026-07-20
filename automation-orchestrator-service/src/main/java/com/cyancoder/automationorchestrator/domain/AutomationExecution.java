package com.cyancoder.automationorchestrator.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Document("automation_executions")
@CompoundIndex(name = "automation_execution_scope_key", def = "{'tenantKey':1,'siteKey':1,'executionId':1}", unique = true)
public class AutomationExecution {
    @Id
    private String id;
    private String executionId;
    private String blockKey;
    private String automationFlowKey;
    private Integer flowVersion;
    private String entryType;
    private String managedObjectId;
    private String idempotencyKey;
    private AutomationExecutionMode executionMode = AutomationExecutionMode.ASYNC;
    private AutomationFailurePolicy failurePolicy = AutomationFailurePolicy.FAIL_FAST;
    private String correlationKey;
    private String tenantKey;
    private String siteKey;
    private String status;
    private Map<String, Object> input = new LinkedHashMap<>();
    private Map<String, Object> context = new LinkedHashMap<>();
    private Map<String, Object> inlineFragment = new LinkedHashMap<>();
    private Map<String, Object> output = new LinkedHashMap<>();
    private Map<String, Object> snapshot = new LinkedHashMap<>();
    private Map<String, Object> error = new LinkedHashMap<>();
    private String currentNodeId;
    private String currentConcurrencyKey;
    private String resumeNodeId;
    private Instant resumeAt;
    private String callbackPath;
    private List<AutomationExecutionStep> steps = new ArrayList<>();
    private List<Map<String, Object>> deadLetters = new ArrayList<>();
    private String parentExecutionId;
    private Integer maxRetries = 0;
    private Integer retryCount = 0;
    private Long timeoutSeconds;
    private Instant timeoutAt;
    private boolean cancelRequested;
    private Instant cancelledAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }
    public String getBlockKey() { return blockKey; }
    public void setBlockKey(String blockKey) { this.blockKey = blockKey; }
    public String getAutomationFlowKey() { return automationFlowKey; }
    public void setAutomationFlowKey(String automationFlowKey) { this.automationFlowKey = automationFlowKey; }
    public Integer getFlowVersion() { return flowVersion; }
    public void setFlowVersion(Integer flowVersion) { this.flowVersion = flowVersion; }
    public String getEntryType() { return entryType; }
    public void setEntryType(String entryType) { this.entryType = entryType; }
    public String getManagedObjectId() { return managedObjectId; }
    public void setManagedObjectId(String managedObjectId) { this.managedObjectId = managedObjectId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public AutomationExecutionMode getExecutionMode() { return executionMode; }
    public void setExecutionMode(AutomationExecutionMode executionMode) { this.executionMode = executionMode; }
    public AutomationFailurePolicy getFailurePolicy() { return failurePolicy; }
    public void setFailurePolicy(AutomationFailurePolicy failurePolicy) { this.failurePolicy = failurePolicy; }
    public String getCorrelationKey() { return correlationKey; }
    public void setCorrelationKey(String correlationKey) { this.correlationKey = correlationKey; }
    public String getTenantKey() { return tenantKey; }
    public void setTenantKey(String tenantKey) { this.tenantKey = tenantKey; }
    public String getSiteKey() { return siteKey; }
    public void setSiteKey(String siteKey) { this.siteKey = siteKey; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Map<String, Object> getInput() { return input; }
    public void setInput(Map<String, Object> input) { this.input = input; }
    public Map<String, Object> getContext() { return context; }
    public void setContext(Map<String, Object> context) { this.context = context == null ? new LinkedHashMap<>() : new LinkedHashMap<>(context); }
    public Map<String, Object> getInlineFragment() { return inlineFragment; }
    public void setInlineFragment(Map<String, Object> inlineFragment) { this.inlineFragment = inlineFragment; }
    public Map<String, Object> getOutput() { return output; }
    public void setOutput(Map<String, Object> output) { this.output = output; }
    public Map<String, Object> getSnapshot() { return snapshot; }
    public void setSnapshot(Map<String, Object> snapshot) { this.snapshot = snapshot; }
    public Map<String, Object> getError() { return error; }
    public void setError(Map<String, Object> error) { this.error = error; }
    public String getCurrentNodeId() { return currentNodeId; }
    public void setCurrentNodeId(String currentNodeId) { this.currentNodeId = currentNodeId; }
    public String getCurrentConcurrencyKey() { return currentConcurrencyKey; }
    public void setCurrentConcurrencyKey(String currentConcurrencyKey) { this.currentConcurrencyKey = currentConcurrencyKey; }
    public String getResumeNodeId() { return resumeNodeId; }
    public void setResumeNodeId(String resumeNodeId) { this.resumeNodeId = resumeNodeId; }
    public Instant getResumeAt() { return resumeAt; }
    public void setResumeAt(Instant resumeAt) { this.resumeAt = resumeAt; }
    public String getCallbackPath() { return callbackPath; }
    public void setCallbackPath(String callbackPath) { this.callbackPath = callbackPath; }
    public List<AutomationExecutionStep> getSteps() { return steps; }
    public void setSteps(List<AutomationExecutionStep> steps) { this.steps = steps == null ? new ArrayList<>() : new ArrayList<>(steps); }
    public List<Map<String, Object>> getDeadLetters() { return deadLetters; }
    public void setDeadLetters(List<Map<String, Object>> deadLetters) { this.deadLetters = deadLetters == null ? new ArrayList<>() : new ArrayList<>(deadLetters); }
    public String getParentExecutionId() { return parentExecutionId; }
    public void setParentExecutionId(String parentExecutionId) { this.parentExecutionId = parentExecutionId; }
    public Integer getMaxRetries() { return maxRetries; }
    public void setMaxRetries(Integer maxRetries) { this.maxRetries = maxRetries; }
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public Long getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(Long timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    public Instant getTimeoutAt() { return timeoutAt; }
    public void setTimeoutAt(Instant timeoutAt) { this.timeoutAt = timeoutAt; }
    public boolean isCancelRequested() { return cancelRequested; }
    public void setCancelRequested(boolean cancelRequested) { this.cancelRequested = cancelRequested; }
    public Instant getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
