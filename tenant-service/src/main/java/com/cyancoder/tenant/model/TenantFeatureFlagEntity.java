package com.cyancoder.tenant.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tenant_feature_flags")
public class TenantFeatureFlagEntity {
    @Id
    @Column(name = "flag_id", nullable = false, updatable = false, length = 220)
    private String flagId;
    @Column(name = "tenant_key", nullable = false, length = 80)
    private String tenantKey;
    @Column(name = "flag_key", nullable = false, length = 100)
    private String flagKey;
    @Column(name = "flag_value", nullable = false, length = 1000)
    private String flagValue;

    public String getFlagId() { return flagId; }
    public void setFlagId(String flagId) { this.flagId = flagId; }
    public String getTenantKey() { return tenantKey; }
    public void setTenantKey(String tenantKey) { this.tenantKey = tenantKey; }
    public String getFlagKey() { return flagKey; }
    public void setFlagKey(String flagKey) { this.flagKey = flagKey; }
    public String getFlagValue() { return flagValue; }
    public void setFlagValue(String flagValue) { this.flagValue = flagValue; }
}
