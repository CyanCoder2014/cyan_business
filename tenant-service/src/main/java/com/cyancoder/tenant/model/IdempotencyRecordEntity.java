package com.cyancoder.tenant.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "tenant_idempotency_records")
public class IdempotencyRecordEntity {
    @Id
    @Column(name = "record_id", nullable = false, updatable = false, length = 500)
    private String recordId;
    @Column(name = "resource_key", nullable = false, length = 180)
    private String resourceKey;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }
    public String getResourceKey() { return resourceKey; }
    public void setResourceKey(String resourceKey) { this.resourceKey = resourceKey; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
