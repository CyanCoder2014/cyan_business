package com.cyancoder.aiorchestrator.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Document("ai_conversation_sessions")
@CompoundIndex(name = "ai_session_scope_unique", def = "{'tenantKey':1,'siteKey':1,'sessionId':1}", unique = true)
public class ConversationSession {
    @Id
    private String id;
    private String sessionId;
    private String channelType;
    private String tenantKey;
    private String siteKey;
    private String clientKey;
    private String draftId;
    private String appTypeHint;
    private SessionStatus status;
    private List<SessionMessage> messages = new ArrayList<>();
    private Map<String, Object> extractedAnswers = new LinkedHashMap<>();
    private List<String> pendingQuestionKeys = new ArrayList<>();
    private List<String> pendingQuestions = new ArrayList<>();
    private String latestPrompt;
    private String latestQuestion;
    private Instant createdAt;
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getChannelType() { return channelType; }
    public void setChannelType(String channelType) { this.channelType = channelType; }
    public String getTenantKey() { return tenantKey; }
    public void setTenantKey(String tenantKey) { this.tenantKey = tenantKey; }
    public String getSiteKey() { return siteKey; }
    public void setSiteKey(String siteKey) { this.siteKey = siteKey; }
    public String getClientKey() { return clientKey; }
    public void setClientKey(String clientKey) { this.clientKey = clientKey; }
    public String getDraftId() { return draftId; }
    public void setDraftId(String draftId) { this.draftId = draftId; }
    public String getAppTypeHint() { return appTypeHint; }
    public void setAppTypeHint(String appTypeHint) { this.appTypeHint = appTypeHint; }
    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }
    public List<SessionMessage> getMessages() { return messages; }
    public void setMessages(List<SessionMessage> messages) { this.messages = messages; }
    public Map<String, Object> getExtractedAnswers() { return extractedAnswers; }
    public void setExtractedAnswers(Map<String, Object> extractedAnswers) { this.extractedAnswers = extractedAnswers; }
    public List<String> getPendingQuestionKeys() { return pendingQuestionKeys; }
    public void setPendingQuestionKeys(List<String> pendingQuestionKeys) { this.pendingQuestionKeys = pendingQuestionKeys; }
    public List<String> getPendingQuestions() { return pendingQuestions; }
    public void setPendingQuestions(List<String> pendingQuestions) { this.pendingQuestions = pendingQuestions; }
    public String getLatestPrompt() { return latestPrompt; }
    public void setLatestPrompt(String latestPrompt) { this.latestPrompt = latestPrompt; }
    public String getLatestQuestion() { return latestQuestion; }
    public void setLatestQuestion(String latestQuestion) { this.latestQuestion = latestQuestion; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
