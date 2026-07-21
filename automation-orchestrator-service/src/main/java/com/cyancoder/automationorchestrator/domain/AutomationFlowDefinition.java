package com.cyancoder.automationorchestrator.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Document("automation_flow_definitions")
@CompoundIndex(name = "automation_flow_scope_version", def = "{'tenantKey':1,'siteKey':1,'flowKey':1,'version':1}", unique = true)
public class AutomationFlowDefinition {
    @Id private String id;
    @Version private Long revision;
    private String tenantKey;
    private String siteKey;
    private String flowKey;
    private Integer version = 1;
    private String name;
    private boolean active;
    private String entryNodeId;
    private String runtimeMode = "VARIABLES";
    private List<AutomationNode> nodes = new ArrayList<>();
    private List<AutomationEdge> edges = new ArrayList<>();
    private Map<String, Object> inputsSchema = new LinkedHashMap<>();
    private Map<String, Object> outputsSchema = new LinkedHashMap<>();
    private List<String> labels = new ArrayList<>();
    private String environment = "default";
    private String lifecycleStatus = "DRAFT";
    private List<String> requiredRoles = new ArrayList<>();
    private Map<String, Object> settings = new LinkedHashMap<>();
    private Map<String, Object> pinData = new LinkedHashMap<>();
    private String errorWorkflowKey;
    private Instant nextScheduledAt;
    private Instant lastScheduledAt;
    private String createdBy;
    private String approvedBy;
    private Instant approvedAt;
    private String publishedBy;
    private Instant publishedAt;
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Long getRevision() { return revision; }
    public void setRevision(Long revision) { this.revision = revision; }
    public String getTenantKey() { return tenantKey; }
    public void setTenantKey(String tenantKey) { this.tenantKey = tenantKey; }
    public String getSiteKey() { return siteKey; }
    public void setSiteKey(String siteKey) { this.siteKey = siteKey; }
    public String getFlowKey() { return flowKey; }
    public void setFlowKey(String flowKey) { this.flowKey = flowKey; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getEntryNodeId() { return entryNodeId; }
    public void setEntryNodeId(String entryNodeId) { this.entryNodeId = entryNodeId; }
    public String getRuntimeMode() { return runtimeMode; }
    public void setRuntimeMode(String runtimeMode) { this.runtimeMode = runtimeMode; }
    public List<AutomationNode> getNodes() { return nodes; }
    public void setNodes(List<AutomationNode> nodes) { this.nodes = nodes == null ? new ArrayList<>() : new ArrayList<>(nodes); }
    public List<AutomationEdge> getEdges() { return edges; }
    public void setEdges(List<AutomationEdge> edges) { this.edges = edges == null ? new ArrayList<>() : new ArrayList<>(edges); }
    public Map<String, Object> getInputsSchema() { return inputsSchema; }
    public void setInputsSchema(Map<String, Object> inputsSchema) { this.inputsSchema = inputsSchema == null ? new LinkedHashMap<>() : new LinkedHashMap<>(inputsSchema); }
    public Map<String, Object> getOutputsSchema() { return outputsSchema; }
    public void setOutputsSchema(Map<String, Object> outputsSchema) { this.outputsSchema = outputsSchema == null ? new LinkedHashMap<>() : new LinkedHashMap<>(outputsSchema); }
    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels == null ? new ArrayList<>() : new ArrayList<>(labels); }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public String getLifecycleStatus() { return lifecycleStatus; }
    public void setLifecycleStatus(String lifecycleStatus) { this.lifecycleStatus = lifecycleStatus; }
    public List<String> getRequiredRoles() { return requiredRoles; }
    public void setRequiredRoles(List<String> requiredRoles) { this.requiredRoles = requiredRoles == null ? new ArrayList<>() : new ArrayList<>(requiredRoles); }
    public Map<String, Object> getSettings() { return settings; }
    public void setSettings(Map<String, Object> settings) { this.settings = settings == null ? new LinkedHashMap<>() : new LinkedHashMap<>(settings); }
    public Map<String, Object> getPinData() { return pinData; }
    public void setPinData(Map<String, Object> pinData) { this.pinData = pinData == null ? new LinkedHashMap<>() : new LinkedHashMap<>(pinData); }
    public String getErrorWorkflowKey() { return errorWorkflowKey; }
    public void setErrorWorkflowKey(String errorWorkflowKey) { this.errorWorkflowKey = errorWorkflowKey; }
    public Instant getNextScheduledAt() { return nextScheduledAt; }
    public void setNextScheduledAt(Instant nextScheduledAt) { this.nextScheduledAt = nextScheduledAt; }
    public Instant getLastScheduledAt() { return lastScheduledAt; }
    public void setLastScheduledAt(Instant lastScheduledAt) { this.lastScheduledAt = lastScheduledAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }
    public String getPublishedBy() { return publishedBy; }
    public void setPublishedBy(String publishedBy) { this.publishedBy = publishedBy; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
