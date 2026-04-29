package com.cyancoder.commerce.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "commerce_outbox_events")
public class CommerceOutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String eventKey;
    private String sourceService;
    private String entityType;
    private String entityKey;
    private String actionType;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String payloadJson;
    private String status;
    private Integer attemptCount;
    private Instant createdAt;
    private Instant lastAttemptAt;
    private Instant deliveredAt;
    @PrePersist
    public void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (attemptCount == null) attemptCount = 0;
        if (status == null) status = "PENDING";
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEventKey() { return eventKey; }
    public void setEventKey(String eventKey) { this.eventKey = eventKey; }
    public String getSourceService() { return sourceService; }
    public void setSourceService(String sourceService) { this.sourceService = sourceService; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getEntityKey() { return entityKey; }
    public void setEntityKey(String entityKey) { this.entityKey = entityKey; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getAttemptCount() { return attemptCount; }
    public void setAttemptCount(Integer attemptCount) { this.attemptCount = attemptCount; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getLastAttemptAt() { return lastAttemptAt; }
    public void setLastAttemptAt(Instant lastAttemptAt) { this.lastAttemptAt = lastAttemptAt; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }
}
