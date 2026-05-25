package com.cyancoder.botadapter.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("bot_chat_session_mappings")
@CompoundIndex(name = "uk_bot_chat_mapping", def = "{'channel':1,'integrationKey':1,'externalChatId':1}", unique = true)
public class BotChatSessionMapping {
    @Id
    private String id;
    private BotChannel channel;
    private String integrationKey;
    private String externalChatId;
    private String sessionId;
    private Instant createdAt;
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public BotChannel getChannel() { return channel; }
    public void setChannel(BotChannel channel) { this.channel = channel; }
    public String getIntegrationKey() { return integrationKey; }
    public void setIntegrationKey(String integrationKey) { this.integrationKey = integrationKey; }
    public String getExternalChatId() { return externalChatId; }
    public void setExternalChatId(String externalChatId) { this.externalChatId = externalChatId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
