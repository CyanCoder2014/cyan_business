package com.cyancoder.bpm.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import org.springframework.data.annotation.Version;

@Document("bpm_flow_definitions")
@CompoundIndex(name = "bpm_flow_scope_key", def = "{'tenantKey':1,'siteKey':1,'flowKey':1,'version':1}", unique = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DynamicFlowDefinition {
    @Id
    private String id;
    @Version
    private Long revision;
    private String tenantKey;
    private String siteKey;
    private String flowKey;
    private Integer version;
    private String name;
    private String description;
    private String startState;
    private List<FlowState> states;
    private List<FlowTransition> transitions;
    private boolean active;
    private String lifecycleStatus = "DRAFT";
    private Map<String, Object> layout = new LinkedHashMap<>();
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
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStartState() { return startState; }
    public void setStartState(String startState) { this.startState = startState; }
    public List<FlowState> getStates() { return states; }
    public void setStates(List<FlowState> states) { this.states = states; }
    public List<FlowTransition> getTransitions() { return transitions; }
    public void setTransitions(List<FlowTransition> transitions) { this.transitions = transitions; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getLifecycleStatus() { return lifecycleStatus; }
    public void setLifecycleStatus(String lifecycleStatus) { this.lifecycleStatus = lifecycleStatus; }
    public Map<String, Object> getLayout() { return layout; }
    public void setLayout(Map<String, Object> layout) { this.layout = layout == null ? new LinkedHashMap<>() : new LinkedHashMap<>(layout); }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
