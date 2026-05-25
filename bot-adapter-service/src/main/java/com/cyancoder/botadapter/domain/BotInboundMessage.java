package com.cyancoder.botadapter.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document("bot_inbound_messages")
@CompoundIndex(name = "uk_bot_message", def = "{'channel':1,'integrationKey':1,'externalMessageId':1}", unique = true)
public class BotInboundMessage {
    @Id
    private String id;
    private BotChannel channel;
    private String integrationKey;
    private String externalMessageId;
    private String externalChatId;
    private String sessionId;
    private String text;
    private String status;
    private Map<String, Object> rawPayload;
    private Instant receivedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public BotChannel getChannel() { return channel; }
    public void setChannel(BotChannel channel) { this.channel = channel; }
    public String getIntegrationKey() { return integrationKey; }
    public void setIntegrationKey(String integrationKey) { this.integrationKey = integrationKey; }
    public String getExternalMessageId() { return externalMessageId; }
    public void setExternalMessageId(String externalMessageId) { this.externalMessageId = externalMessageId; }
    public String getExternalChatId() { return externalChatId; }
    public void setExternalChatId(String externalChatId) { this.externalChatId = externalChatId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Map<String, Object> getRawPayload() { return rawPayload; }
    public void setRawPayload(Map<String, Object> rawPayload) { this.rawPayload = rawPayload; }
    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }
}
