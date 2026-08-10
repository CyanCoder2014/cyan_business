package com.cyancoder.botadapter.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Document("bot_process_bindings")
@CompoundIndex(name = "uk_bot_process_binding", def = "{'channel':1,'integrationKey':1,'bindingKey':1}", unique = true)
public class BotProcessBinding {
    @Id private String id;
    private BotChannel channel;
    private String integrationKey;
    private String bindingKey;
    private String tenantKey;
    private String siteKey;
    private BotProcessTriggerType triggerType = BotProcessTriggerType.EVERY_MESSAGE;
    private String commandPrefix;
    private BotProcessTargetType targetType;
    private String targetKey;
    private Map<String, Object> inputTemplate = new LinkedHashMap<>();
    private boolean enabled = true;
    private Instant createdAt;
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public BotChannel getChannel() { return channel; }
    public void setChannel(BotChannel channel) { this.channel = channel; }
    public String getIntegrationKey() { return integrationKey; }
    public void setIntegrationKey(String integrationKey) { this.integrationKey = integrationKey; }
    public String getBindingKey() { return bindingKey; }
    public void setBindingKey(String bindingKey) { this.bindingKey = bindingKey; }
    public String getTenantKey() { return tenantKey; }
    public void setTenantKey(String tenantKey) { this.tenantKey = tenantKey; }
    public String getSiteKey() { return siteKey; }
    public void setSiteKey(String siteKey) { this.siteKey = siteKey; }
    public BotProcessTriggerType getTriggerType() { return triggerType; }
    public void setTriggerType(BotProcessTriggerType triggerType) { this.triggerType = triggerType; }
    public String getCommandPrefix() { return commandPrefix; }
    public void setCommandPrefix(String commandPrefix) { this.commandPrefix = commandPrefix; }
    public BotProcessTargetType getTargetType() { return targetType; }
    public void setTargetType(BotProcessTargetType targetType) { this.targetType = targetType; }
    public String getTargetKey() { return targetKey; }
    public void setTargetKey(String targetKey) { this.targetKey = targetKey; }
    public Map<String, Object> getInputTemplate() { return inputTemplate; }
    public void setInputTemplate(Map<String, Object> inputTemplate) { this.inputTemplate = inputTemplate == null ? new LinkedHashMap<>() : inputTemplate; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
