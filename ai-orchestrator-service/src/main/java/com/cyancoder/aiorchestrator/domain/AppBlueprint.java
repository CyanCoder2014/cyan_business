package com.cyancoder.aiorchestrator.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Document("ai_app_blueprints")
@CompoundIndex(name = "ai_blueprint_key_version", def = "{'blueprintKey':1,'version':1}", unique = true)
public class AppBlueprint {
    @Id
    private String id;
    private String blueprintKey;
    private String appType;
    private Integer version;
    private String title;
    private String description;
    private boolean active;
    private List<String> capabilities = new ArrayList<>();
    private List<BlueprintQuestionDefinition> requiredQuestions = new ArrayList<>();
    private Map<String, Object> defaultAnswers = new LinkedHashMap<>();
    private PlatformAppDslDefinition baseDsl = new PlatformAppDslDefinition();
    private Instant createdAt;
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBlueprintKey() { return blueprintKey; }
    public void setBlueprintKey(String blueprintKey) { this.blueprintKey = blueprintKey; }
    public String getAppType() { return appType; }
    public void setAppType(String appType) { this.appType = appType; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<String> getCapabilities() { return capabilities; }
    public void setCapabilities(List<String> capabilities) { this.capabilities = capabilities; }
    public List<BlueprintQuestionDefinition> getRequiredQuestions() { return requiredQuestions; }
    public void setRequiredQuestions(List<BlueprintQuestionDefinition> requiredQuestions) { this.requiredQuestions = requiredQuestions; }
    public Map<String, Object> getDefaultAnswers() { return defaultAnswers; }
    public void setDefaultAnswers(Map<String, Object> defaultAnswers) { this.defaultAnswers = defaultAnswers; }
    public PlatformAppDslDefinition getBaseDsl() { return baseDsl; }
    public void setBaseDsl(PlatformAppDslDefinition baseDsl) { this.baseDsl = baseDsl; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
