package com.cyancoder.tenant.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tenant_capability_overrides")
public class TenantCapabilityOverrideEntity {
    @Id
    @Column(name = "override_id", nullable = false, updatable = false, length = 260)
    private String overrideId;
    @Column(name = "tenant_key", nullable = false, length = 80)
    private String tenantKey;
    @Column(name = "site_key", length = 80)
    private String siteKey;
    @Column(name = "capability_key", nullable = false, length = 80)
    private String capabilityKey;
    @Column(name = "enabled", nullable = false)
    private boolean enabled;
    @Column(name = "reason", length = 400)
    private String reason;

    public String getOverrideId() { return overrideId; }
    public void setOverrideId(String overrideId) { this.overrideId = overrideId; }
    public String getTenantKey() { return tenantKey; }
    public void setTenantKey(String tenantKey) { this.tenantKey = tenantKey; }
    public String getSiteKey() { return siteKey; }
    public void setSiteKey(String siteKey) { this.siteKey = siteKey; }
    public String getCapabilityKey() { return capabilityKey; }
    public void setCapabilityKey(String capabilityKey) { this.capabilityKey = capabilityKey; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
