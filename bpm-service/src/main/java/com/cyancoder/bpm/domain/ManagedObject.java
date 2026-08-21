package com.cyancoder.bpm.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document("bpm_managed_objects")
@CompoundIndex(name = "bpm_managed_object_scope", def = "{'tenantKey':1,'siteKey':1,'objectType':1,'objectRef.service':1,'objectRef.entityKey':1,'objectRef.recordKey':1}")
public class ManagedObject {
    @Id
    private String id;
    @Version
    private Long revision;
    private String tenantKey;
    private String siteKey;
    private String objectType;
    private String title;
    private ManagedObjectRef objectRef;
    private String flowKey;
    private String state;
    private String processInstanceId;
    private String assignee;
    private AssigneeType assigneeType = AssigneeType.USER;
    private Map<String, Object> payload = new HashMap<>();
    private FlowAccessRule accessRule;
    private boolean locked;
    private String lockedBy;
    private String priority = "NORMAL";
    private Instant dueAt;
    private Instant completedAt;
    private List<String> auditLog = new ArrayList<>();
    private List<TransitionHistoryEntry> transitionHistory = new ArrayList<>();
    private List<AsyncActionRegistration> asyncActionRegistry = new ArrayList<>();
    private List<AutomationBlockExecution> automationBlockRegistry = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Long getRevision() { return revision; }
    public void setRevision(Long revision) { this.revision = revision; }
    public String getTenantKey() { return tenantKey; }
    public void setTenantKey(String tenantKey) { this.tenantKey = tenantKey; }
    public String getSiteKey() { return siteKey; }
    public void setSiteKey(String siteKey) { this.siteKey = siteKey; }
    public String getObjectType() { return objectType; }
    public void setObjectType(String objectType) { this.objectType = objectType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public ManagedObjectRef getObjectRef() { return objectRef; }
    public void setObjectRef(ManagedObjectRef objectRef) { this.objectRef = objectRef; }
    public String getFlowKey() { return flowKey; }
    public void setFlowKey(String flowKey) { this.flowKey = flowKey; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getProcessInstanceId() { return processInstanceId; }
    public void setProcessInstanceId(String processInstanceId) { this.processInstanceId = processInstanceId; }
    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
    public AssigneeType getAssigneeType() { return assigneeType == null ? AssigneeType.USER : assigneeType; }
    public void setAssigneeType(AssigneeType assigneeType) { this.assigneeType = assigneeType == null ? AssigneeType.USER : assigneeType; }
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
    public FlowAccessRule getAccessRule() { return accessRule; }
    public void setAccessRule(FlowAccessRule accessRule) { this.accessRule = accessRule; }
    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }
    public String getLockedBy() { return lockedBy; }
    public void setLockedBy(String lockedBy) { this.lockedBy = lockedBy; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority == null || priority.isBlank() ? "NORMAL" : priority; }
    public Instant getDueAt() { return dueAt; }
    public void setDueAt(Instant dueAt) { this.dueAt = dueAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public List<String> getAuditLog() { return auditLog; }
    public void setAuditLog(List<String> auditLog) { this.auditLog = auditLog; }
    public List<TransitionHistoryEntry> getTransitionHistory() { return transitionHistory; }
    public void setTransitionHistory(List<TransitionHistoryEntry> transitionHistory) { this.transitionHistory = transitionHistory; }
    public List<AsyncActionRegistration> getAsyncActionRegistry() { return asyncActionRegistry; }
    public void setAsyncActionRegistry(List<AsyncActionRegistration> asyncActionRegistry) { this.asyncActionRegistry = asyncActionRegistry; }
    public List<AutomationBlockExecution> getAutomationBlockRegistry() { return automationBlockRegistry; }
    public void setAutomationBlockRegistry(List<AutomationBlockExecution> automationBlockRegistry) { this.automationBlockRegistry = automationBlockRegistry; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
