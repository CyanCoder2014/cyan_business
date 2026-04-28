package com.cyancoder.inventoryautomation.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "inventory_automation_actions")
public class InventoryAutomationAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String eventKey;
    private String entityType;
    private String entityKey;
    private String actionType;
    private String automationType;
    @Column(length = 2000)
    private String summary;
    @Lob
    @Column(length = 20000)
    private String payloadJson;
    private Instant createdAt;
    @PrePersist
    public void onCreate() { if (createdAt == null) { createdAt = Instant.now(); } }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEventKey() { return eventKey; }
    public void setEventKey(String eventKey) { this.eventKey = eventKey; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getEntityKey() { return entityKey; }
    public void setEntityKey(String entityKey) { this.entityKey = entityKey; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getAutomationType() { return automationType; }
    public void setAutomationType(String automationType) { this.automationType = automationType; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
