package com.cyancoder.processor.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class ProcessorDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String processorKey;
    private String targetType;
    @Lob
    @Column(length = 20000)
    private String validatorsJson;
    @Lob
    @Column(length = 20000)
    private String operatorsJson;
    private String description;
    private boolean active = true;
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProcessorKey() {
        return processorKey;
    }

    public void setProcessorKey(String processorKey) {
        this.processorKey = processorKey;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getValidatorsJson() {
        return validatorsJson;
    }

    public void setValidatorsJson(String validatorsJson) {
        this.validatorsJson = validatorsJson;
    }

    public String getOperatorsJson() {
        return operatorsJson;
    }

    public void setOperatorsJson(String operatorsJson) {
        this.operatorsJson = operatorsJson;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
