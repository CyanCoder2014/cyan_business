package com.cyancoder.aiorchestrator.domain;

import java.util.LinkedHashMap;
import java.util.Map;

public class ProvisioningStepResult {
    private String stepKey;
    private String serviceKey;
    private String endpointPath;
    private String status;
    private String idempotencyKey;
    private String summary;
    private Map<String, Object> response = new LinkedHashMap<>();

    public String getStepKey() { return stepKey; }
    public void setStepKey(String stepKey) { this.stepKey = stepKey; }
    public String getServiceKey() { return serviceKey; }
    public void setServiceKey(String serviceKey) { this.serviceKey = serviceKey; }
    public String getEndpointPath() { return endpointPath; }
    public void setEndpointPath(String endpointPath) { this.endpointPath = endpointPath; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public Map<String, Object> getResponse() { return response; }
    public void setResponse(Map<String, Object> response) { this.response = response; }
}
