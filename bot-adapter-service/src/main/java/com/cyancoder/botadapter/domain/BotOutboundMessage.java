package com.cyancoder.botadapter.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Document("bot_outbound_messages")
@CompoundIndex(name = "bot_outbound_scope_idx", def = "{'tenantKey':1,'siteKey':1,'integrationKey':1,'updatedAt':-1}")
@CompoundIndex(name = "uk_bot_outbound_idempotency", def = "{'integrationKey':1,'idempotencyKey':1}", unique = true, sparse = true)
public class BotOutboundMessage {
    @Id
    private String id;
    private BotChannel channel;
    private String integrationKey;
    private String tenantKey;
    private String siteKey;
    private String clientKey;
    private String externalChatId;
    private String sessionId;
    private String idempotencyKey;
    private String text;
    private String status;
    private int attemptCount;
    private String errorMessage;
    private Map<String, Object> providerResponse = new LinkedHashMap<>();
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastAttemptAt;
    private Instant deliveredAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public BotChannel getChannel() { return channel; }
    public void setChannel(BotChannel channel) { this.channel = channel; }
    public String getIntegrationKey() { return integrationKey; }
    public void setIntegrationKey(String integrationKey) { this.integrationKey = integrationKey; }
    public String getTenantKey() { return tenantKey; }
    public void setTenantKey(String tenantKey) { this.tenantKey = tenantKey; }
    public String getSiteKey() { return siteKey; }
    public void setSiteKey(String siteKey) { this.siteKey = siteKey; }
    public String getClientKey() { return clientKey; }
    public void setClientKey(String clientKey) { this.clientKey = clientKey; }
    public String getExternalChatId() { return externalChatId; }
    public void setExternalChatId(String externalChatId) { this.externalChatId = externalChatId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Map<String, Object> getProviderResponse() { return providerResponse; }
    public void setProviderResponse(Map<String, Object> providerResponse) { this.providerResponse = providerResponse; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Instant getLastAttemptAt() { return lastAttemptAt; }
    public void setLastAttemptAt(Instant lastAttemptAt) { this.lastAttemptAt = lastAttemptAt; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }
}
