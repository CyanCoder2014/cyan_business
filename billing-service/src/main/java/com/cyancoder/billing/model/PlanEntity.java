package com.cyancoder.billing.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "billing_plans")
public class PlanEntity {
    @Id @Column(name = "plan_key", nullable = false, updatable = false, length = 80)
    private String planKey;
    @Column(name = "display_name", nullable = false, length = 180)
    private String displayName;
    @Column(name = "description", length = 1000)
    private String description;
    @Column(name = "billing_mode", nullable = false, length = 24)
    private String billingMode;
    @Column(name = "active", nullable = false)
    private boolean active;
    @Column(name = "features_json", nullable = false, length = 12000)
    private String featuresJson;
    @Column(name = "limits_json", nullable = false, length = 12000)
    private String limitsJson;

    public String getPlanKey() { return planKey; }
    public void setPlanKey(String planKey) { this.planKey = planKey; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBillingMode() { return billingMode; }
    public void setBillingMode(String billingMode) { this.billingMode = billingMode; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getFeaturesJson() { return featuresJson; }
    public void setFeaturesJson(String featuresJson) { this.featuresJson = featuresJson; }
    public String getLimitsJson() { return limitsJson; }
    public void setLimitsJson(String limitsJson) { this.limitsJson = limitsJson; }
}
