package com.cyancoder.billing.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "tenant_subscriptions")
public class TenantSubscriptionEntity {
    @Id @Column(name = "tenant_key", nullable = false, updatable = false, length = 80)
    private String tenantKey;
    @Column(name = "plan_key", nullable = false, length = 80)
    private String planKey;
    @Column(name = "status", nullable = false, length = 24)
    private String status;
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;
    @Column(name = "renews_at")
    private Instant renewsAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public String getTenantKey() { return tenantKey; }
    public void setTenantKey(String tenantKey) { this.tenantKey = tenantKey; }
    public String getPlanKey() { return planKey; }
    public void setPlanKey(String planKey) { this.planKey = planKey; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getRenewsAt() { return renewsAt; }
    public void setRenewsAt(Instant renewsAt) { this.renewsAt = renewsAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
