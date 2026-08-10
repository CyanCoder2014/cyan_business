package com.cyancoder.notification.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "user_notifications", indexes = {
        @Index(name = "idx_notification_recipient_scope", columnList = "recipient,tenantKey,siteKey,createdAt")
})
public class UserNotificationEntity {
    @Id private String notificationId;
    private String tenantKey;
    private String siteKey;
    private String recipient;
    private String type;
    private String severity;
    private String title;
    private String body;
    private String deepLink;
    private String sourceService;
    private String sourceKey;
    private Instant createdAt;
    private Instant readAt;
    private long version;

    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String value) { notificationId = value; }
    public String getTenantKey() { return tenantKey; }
    public void setTenantKey(String value) { tenantKey = value; }
    public String getSiteKey() { return siteKey; }
    public void setSiteKey(String value) { siteKey = value; }
    public String getRecipient() { return recipient; }
    public void setRecipient(String value) { recipient = value; }
    public String getType() { return type; }
    public void setType(String value) { type = value; }
    public String getSeverity() { return severity; }
    public void setSeverity(String value) { severity = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { title = value; }
    public String getBody() { return body; }
    public void setBody(String value) { body = value; }
    public String getDeepLink() { return deepLink; }
    public void setDeepLink(String value) { deepLink = value; }
    public String getSourceService() { return sourceService; }
    public void setSourceService(String value) { sourceService = value; }
    public String getSourceKey() { return sourceKey; }
    public void setSourceKey(String value) { sourceKey = value; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant value) { createdAt = value; }
    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant value) { readAt = value; }
    public long getVersion() { return version; }
    public void setVersion(long value) { version = value; }
}
