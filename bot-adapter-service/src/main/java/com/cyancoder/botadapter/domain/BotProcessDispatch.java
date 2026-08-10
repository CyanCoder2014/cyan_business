package com.cyancoder.botadapter.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("bot_process_dispatches")
@CompoundIndex(name = "uk_bot_process_dispatch", def = "{'bindingId':1,'inboundMessageId':1}", unique = true)
public class BotProcessDispatch {
    @Id private String id;
    private String bindingId;
    private String bindingKey;
    private String inboundMessageId;
    private String tenantKey;
    private String siteKey;
    private BotProcessTargetType targetType;
    private String targetKey;
    private String targetReference;
    private String status;
    private String errorCode;
    private String errorMessage;
    private Instant createdAt;
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBindingId() { return bindingId; }
    public void setBindingId(String bindingId) { this.bindingId = bindingId; }
    public String getBindingKey() { return bindingKey; }
    public void setBindingKey(String bindingKey) { this.bindingKey = bindingKey; }
    public String getInboundMessageId() { return inboundMessageId; }
    public void setInboundMessageId(String inboundMessageId) { this.inboundMessageId = inboundMessageId; }
    public String getTenantKey() { return tenantKey; }
    public void setTenantKey(String tenantKey) { this.tenantKey = tenantKey; }
    public String getSiteKey() { return siteKey; }
    public void setSiteKey(String siteKey) { this.siteKey = siteKey; }
    public BotProcessTargetType getTargetType() { return targetType; }
    public void setTargetType(BotProcessTargetType targetType) { this.targetType = targetType; }
    public String getTargetKey() { return targetKey; }
    public void setTargetKey(String targetKey) { this.targetKey = targetKey; }
    public String getTargetReference() { return targetReference; }
    public void setTargetReference(String targetReference) { this.targetReference = targetReference; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
