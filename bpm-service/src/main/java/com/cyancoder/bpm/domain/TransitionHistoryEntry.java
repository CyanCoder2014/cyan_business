package com.cyancoder.bpm.domain;

import java.time.Instant;
import java.util.Map;

public class TransitionHistoryEntry {
    private String transitionId;
    private String label;
    private String fromState;
    private String toState;
    private String actorUserId;
    private String decision;
    private String note;
    private String formKey;
    private String processorKey;
    private String submittedFormId;
    private Map<String, Object> submittedFormData;
    private Instant timestamp;

    public String getTransitionId() { return transitionId; }
    public void setTransitionId(String transitionId) { this.transitionId = transitionId; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getFromState() { return fromState; }
    public void setFromState(String fromState) { this.fromState = fromState; }
    public String getToState() { return toState; }
    public void setToState(String toState) { this.toState = toState; }
    public String getActorUserId() { return actorUserId; }
    public void setActorUserId(String actorUserId) { this.actorUserId = actorUserId; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getFormKey() { return formKey; }
    public void setFormKey(String formKey) { this.formKey = formKey; }
    public String getProcessorKey() { return processorKey; }
    public void setProcessorKey(String processorKey) { this.processorKey = processorKey; }
    public String getSubmittedFormId() { return submittedFormId; }
    public void setSubmittedFormId(String submittedFormId) { this.submittedFormId = submittedFormId; }
    public Map<String, Object> getSubmittedFormData() { return submittedFormData; }
    public void setSubmittedFormData(Map<String, Object> submittedFormData) { this.submittedFormData = submittedFormData; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}

