package com.cyancoder.botadapter.domain;

import org.springframework.data.annotation.Id;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Document("bot_channel_integrations")
@CompoundIndex(name = "uk_bot_integration", def = "{'channel':1,'integrationKey':1}", unique = true)
public class BotChannelIntegration {
    @Id
    private String id;
    private BotChannel channel;
    private String integrationKey;
    private String tenantKey;
    private String siteKey;
    private String clientKey;
    private String appTypeHint;
    private String botId;
    private String botUsername;
    @JsonIgnore
    private String managedBotToken;
    private String tokenSecretRef;
    private String tokenFingerprint;
    private String webhookSecret;
    private String miniAppUrl;
    private boolean miniAppEnabled;
    private String miniAppStartParam;
    private Map<String, Object> providerConfig = new LinkedHashMap<>();
    private boolean active = true;
    private Instant createdAt;
    private Instant updatedAt;

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
    public String getAppTypeHint() { return appTypeHint; }
    public void setAppTypeHint(String appTypeHint) { this.appTypeHint = appTypeHint; }
    public String getBotId() { return botId; }
    public void setBotId(String botId) { this.botId = botId; }
    public String getBotUsername() { return botUsername; }
    public void setBotUsername(String botUsername) { this.botUsername = botUsername; }
    public String getManagedBotToken() { return managedBotToken; }
    public void setManagedBotToken(String managedBotToken) { this.managedBotToken = managedBotToken; }
    public String getTokenSecretRef() { return tokenSecretRef; }
    public void setTokenSecretRef(String tokenSecretRef) { this.tokenSecretRef = tokenSecretRef; }
    public String getTokenFingerprint() { return tokenFingerprint; }
    public void setTokenFingerprint(String tokenFingerprint) { this.tokenFingerprint = tokenFingerprint; }
    public String getWebhookSecret() { return webhookSecret; }
    public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }
    public String getMiniAppUrl() { return miniAppUrl; }
    public void setMiniAppUrl(String miniAppUrl) { this.miniAppUrl = miniAppUrl; }
    public boolean isMiniAppEnabled() { return miniAppEnabled; }
    public void setMiniAppEnabled(boolean miniAppEnabled) { this.miniAppEnabled = miniAppEnabled; }
    public String getMiniAppStartParam() { return miniAppStartParam; }
    public void setMiniAppStartParam(String miniAppStartParam) { this.miniAppStartParam = miniAppStartParam; }
    public Map<String, Object> getProviderConfig() { return providerConfig; }
    public void setProviderConfig(Map<String, Object> providerConfig) { this.providerConfig = providerConfig; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
