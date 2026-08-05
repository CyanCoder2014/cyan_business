package com.cyancoder.billing.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "billing_idempotency_records")
public class BillingIdempotencyEntity {
    @Id @Column(name = "record_id", nullable = false, updatable = false, length = 500)
    private String recordId;
    @Column(name = "tenant_key", nullable = false, length = 80)
    private String tenantKey;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }
    public String getTenantKey() { return tenantKey; }
    public void setTenantKey(String tenantKey) { this.tenantKey = tenantKey; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
