package com.cyancoder.bpm.domain;

public class AsyncActionRegistration {
    private String actionKey;
    private String correlationKey;
    private String stateId;
    private String status;

    public String getActionKey() { return actionKey; }
    public void setActionKey(String actionKey) { this.actionKey = actionKey; }
    public String getCorrelationKey() { return correlationKey; }
    public void setCorrelationKey(String correlationKey) { this.correlationKey = correlationKey; }
    public String getStateId() { return stateId; }
    public void setStateId(String stateId) { this.stateId = stateId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

