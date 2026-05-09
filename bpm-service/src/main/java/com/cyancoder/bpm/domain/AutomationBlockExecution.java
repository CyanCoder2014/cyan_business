package com.cyancoder.bpm.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AutomationBlockExecution {
    private String blockKey;
    private String automationFlowKey;
    private AutomationExecutionMode executionMode = AutomationExecutionMode.ASYNC;
    private AutomationFailurePolicy failurePolicy = AutomationFailurePolicy.MARK_FAILED;
    private String stateId;
    private String correlationKey;
    private boolean waitForCompletion = true;
    private String status;
    private String serviceKey;
    private String path;
    private String method;
    private Map<String, Object> requestBody = new LinkedHashMap<>();
    private Map<String, Object> inlineFragment = new LinkedHashMap<>();
    private Map<String, Object> startResponseMappings = new LinkedHashMap<>();
    private String storeStartResponseAt;
    private Map<String, Object> outputMappings = new LinkedHashMap<>();
    private String storeOutputAt;
    private String nextStateOnSuccess;
    private String nextStateOnFailure;
    private Integer maxRetries = 0;
    private Integer retryCount = 0;
    private Long timeoutSeconds;
    private Instant timeoutAt;
    private boolean cancelRequested;
    private Instant cancelledAt;
    private Instant startedAt;
    private Instant updatedAt;
    private Instant finishedAt;
    private Map<String, Object> snapshot = new LinkedHashMap<>();
    private Map<String, Object> output = new LinkedHashMap<>();
    private Map<String, Object> error = new LinkedHashMap<>();
    private List<String> processedCallbacks = new ArrayList<>();

    public String getBlockKey() { return blockKey; }
    public void setBlockKey(String blockKey) { this.blockKey = blockKey; }
    public String getAutomationFlowKey() { return automationFlowKey; }
    public void setAutomationFlowKey(String automationFlowKey) { this.automationFlowKey = automationFlowKey; }
    public AutomationExecutionMode getExecutionMode() { return executionMode; }
    public void setExecutionMode(AutomationExecutionMode executionMode) { this.executionMode = executionMode; }
    public AutomationFailurePolicy getFailurePolicy() { return failurePolicy; }
    public void setFailurePolicy(AutomationFailurePolicy failurePolicy) { this.failurePolicy = failurePolicy; }
    public String getStateId() { return stateId; }
    public void setStateId(String stateId) { this.stateId = stateId; }
    public String getCorrelationKey() { return correlationKey; }
    public void setCorrelationKey(String correlationKey) { this.correlationKey = correlationKey; }
    public boolean isWaitForCompletion() { return waitForCompletion; }
    public void setWaitForCompletion(boolean waitForCompletion) { this.waitForCompletion = waitForCompletion; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getServiceKey() { return serviceKey; }
    public void setServiceKey(String serviceKey) { this.serviceKey = serviceKey; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public Map<String, Object> getRequestBody() { return requestBody; }
    public void setRequestBody(Map<String, Object> requestBody) { this.requestBody = requestBody; }
    public Map<String, Object> getInlineFragment() { return inlineFragment; }
    public void setInlineFragment(Map<String, Object> inlineFragment) { this.inlineFragment = inlineFragment; }
    public Map<String, Object> getStartResponseMappings() { return startResponseMappings; }
    public void setStartResponseMappings(Map<String, Object> startResponseMappings) { this.startResponseMappings = startResponseMappings; }
    public String getStoreStartResponseAt() { return storeStartResponseAt; }
    public void setStoreStartResponseAt(String storeStartResponseAt) { this.storeStartResponseAt = storeStartResponseAt; }
    public Map<String, Object> getOutputMappings() { return outputMappings; }
    public void setOutputMappings(Map<String, Object> outputMappings) { this.outputMappings = outputMappings; }
    public String getStoreOutputAt() { return storeOutputAt; }
    public void setStoreOutputAt(String storeOutputAt) { this.storeOutputAt = storeOutputAt; }
    public String getNextStateOnSuccess() { return nextStateOnSuccess; }
    public void setNextStateOnSuccess(String nextStateOnSuccess) { this.nextStateOnSuccess = nextStateOnSuccess; }
    public String getNextStateOnFailure() { return nextStateOnFailure; }
    public void setNextStateOnFailure(String nextStateOnFailure) { this.nextStateOnFailure = nextStateOnFailure; }
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
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }
    public Map<String, Object> getSnapshot() { return snapshot; }
    public void setSnapshot(Map<String, Object> snapshot) { this.snapshot = snapshot; }
    public Map<String, Object> getOutput() { return output; }
    public void setOutput(Map<String, Object> output) { this.output = output; }
    public Map<String, Object> getError() { return error; }
    public void setError(Map<String, Object> error) { this.error = error; }
    public List<String> getProcessedCallbacks() { return processedCallbacks; }
    public void setProcessedCallbacks(List<String> processedCallbacks) { this.processedCallbacks = processedCallbacks; }
}
