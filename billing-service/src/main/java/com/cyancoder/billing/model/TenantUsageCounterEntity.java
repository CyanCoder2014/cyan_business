package com.cyancoder.billing.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "tenant_usage_counters")
public class TenantUsageCounterEntity {
    @EmbeddedId
    private Key id;
    @Column(name = "counter_value", nullable = false)
    private long counterValue;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Key getId() { return id; }
    public void setId(Key id) { this.id = id; }
    public long getCounterValue() { return counterValue; }
    public void setCounterValue(long counterValue) { this.counterValue = counterValue; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Embeddable
    public static class Key implements Serializable {
        @Column(name = "tenant_key", nullable = false, length = 80)
        private String tenantKey;
        @Column(name = "metric_key", nullable = false, length = 80)
        private String metricKey;

        public Key() {}
        public Key(String tenantKey, String metricKey) { this.tenantKey = tenantKey; this.metricKey = metricKey; }

        public String getTenantKey() { return tenantKey; }
        public void setTenantKey(String tenantKey) { this.tenantKey = tenantKey; }
        public String getMetricKey() { return metricKey; }
        public void setMetricKey(String metricKey) { this.metricKey = metricKey; }

        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof Key key)) return false; return Objects.equals(tenantKey, key.tenantKey) && Objects.equals(metricKey, key.metricKey); }
        @Override public int hashCode() { return Objects.hash(tenantKey, metricKey); }
    }
}
