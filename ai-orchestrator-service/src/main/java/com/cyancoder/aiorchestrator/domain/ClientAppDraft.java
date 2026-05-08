package com.cyancoder.aiorchestrator.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Document("ai_client_app_drafts")
@CompoundIndex(name = "ai_draft_scope_unique", def = "{'tenantKey':1,'siteKey':1,'draftId':1}", unique = true)
public class ClientAppDraft {
    @Id
    private String id;
    private String draftId;
    private String tenantKey;
    private String siteKey;
    private String clientKey;
    private String blueprintKey;
    private Integer blueprintVersion;
    private DraftStatus status;
    private String title;
    private String appType;
    private String latestIntent;
    private Map<String, Object> answers = new LinkedHashMap<>();
    private PlatformAppDslDefinition resolvedDsl = new PlatformAppDslDefinition();
    private List<String> pendingQuestionKeys = new ArrayList<>();
    private List<String> pendingQuestions = new ArrayList<>();
    private List<String> manualActions = new ArrayList<>();
    private String latestSessionId;
    private Integer revision;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDraftId() { return draftId; }
    public void setDraftId(String draftId) { this.draftId = draftId; }
    public String getTenantKey() { return tenantKey; }
    public void setTenantKey(String tenantKey) { this.tenantKey = tenantKey; }
    public String getSiteKey() { return siteKey; }
    public void setSiteKey(String siteKey) { this.siteKey = siteKey; }
    public String getClientKey() { return clientKey; }
    public void setClientKey(String clientKey) { this.clientKey = clientKey; }
    public String getBlueprintKey() { return blueprintKey; }
    public void setBlueprintKey(String blueprintKey) { this.blueprintKey = blueprintKey; }
    public Integer getBlueprintVersion() { return blueprintVersion; }
    public void setBlueprintVersion(Integer blueprintVersion) { this.blueprintVersion = blueprintVersion; }
    public DraftStatus getStatus() { return status; }
    public void setStatus(DraftStatus status) { this.status = status; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAppType() { return appType; }
    public void setAppType(String appType) { this.appType = appType; }
    public String getLatestIntent() { return latestIntent; }
    public void setLatestIntent(String latestIntent) { this.latestIntent = latestIntent; }
    public Map<String, Object> getAnswers() { return answers; }
    public void setAnswers(Map<String, Object> answers) { this.answers = answers; }
    public PlatformAppDslDefinition getResolvedDsl() { return resolvedDsl; }
    public void setResolvedDsl(PlatformAppDslDefinition resolvedDsl) { this.resolvedDsl = resolvedDsl; }
    public List<String> getPendingQuestionKeys() { return pendingQuestionKeys; }
    public void setPendingQuestionKeys(List<String> pendingQuestionKeys) { this.pendingQuestionKeys = pendingQuestionKeys; }
    public List<String> getPendingQuestions() { return pendingQuestions; }
    public void setPendingQuestions(List<String> pendingQuestions) { this.pendingQuestions = pendingQuestions; }
    public List<String> getManualActions() { return manualActions; }
    public void setManualActions(List<String> manualActions) { this.manualActions = manualActions; }
    public String getLatestSessionId() { return latestSessionId; }
    public void setLatestSessionId(String latestSessionId) { this.latestSessionId = latestSessionId; }
    public Integer getRevision() { return revision; }
    public void setRevision(Integer revision) { this.revision = revision; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
