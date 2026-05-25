package com.cyancoder.botadapter.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Document("bot_mini_app_builds")
@CompoundIndex(name = "uk_bot_mini_app_build", def = "{'channel':1,'integrationKey':1,'buildKey':1}", unique = true)
public class BotMiniAppBuild {
    @Id
    private String id;
    private BotChannel channel;
    private String integrationKey;
    private String buildKey;
    private String tenantKey;
    private String siteKey;
    private String title;
    private String status;
    private String launchUrl;
    private String publishedUrl;
    private Map<String, Object> manifest = new LinkedHashMap<>();
    private Instant createdAt;
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public BotChannel getChannel() { return channel; }
    public void setChannel(BotChannel channel) { this.channel = channel; }
    public String getIntegrationKey() { return integrationKey; }
    public void setIntegrationKey(String integrationKey) { this.integrationKey = integrationKey; }
    public String getBuildKey() { return buildKey; }
    public void setBuildKey(String buildKey) { this.buildKey = buildKey; }
    public String getTenantKey() { return tenantKey; }
    public void setTenantKey(String tenantKey) { this.tenantKey = tenantKey; }
    public String getSiteKey() { return siteKey; }
    public void setSiteKey(String siteKey) { this.siteKey = siteKey; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLaunchUrl() { return launchUrl; }
    public void setLaunchUrl(String launchUrl) { this.launchUrl = launchUrl; }
    public String getPublishedUrl() { return publishedUrl; }
    public void setPublishedUrl(String publishedUrl) { this.publishedUrl = publishedUrl; }
    public Map<String, Object> getManifest() { return manifest; }
    public void setManifest(Map<String, Object> manifest) { this.manifest = manifest; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
