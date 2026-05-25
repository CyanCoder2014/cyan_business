package com.cyancoder.payment.entity;

import com.cyancoder.payment.domain.PaymentFlowType;
import com.cyancoder.payment.domain.PaymentProviderCode;
import com.cyancoder.payment.domain.PaymentRegion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "payment_method")
public class PaymentMethodEntity extends BaseAuditEntity {

    @Column(nullable = false, unique = true, length = 120)
    private String methodKey;

    @Column(nullable = false, length = 180)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PaymentProviderCode providerCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private PaymentRegion region;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private PaymentFlowType flowType;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private int priorityOrder;

    @Column(columnDefinition = "TEXT")
    private String supportedCurrenciesJson;

    @Column(columnDefinition = "TEXT")
    private String configurationJson;

    @Column(length = 500)
    private String description;

    public String getMethodKey() {
        return methodKey;
    }

    public void setMethodKey(String methodKey) {
        this.methodKey = methodKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public PaymentProviderCode getProviderCode() {
        return providerCode;
    }

    public void setProviderCode(PaymentProviderCode providerCode) {
        this.providerCode = providerCode;
    }

    public PaymentRegion getRegion() {
        return region;
    }

    public void setRegion(PaymentRegion region) {
        this.region = region;
    }

    public PaymentFlowType getFlowType() {
        return flowType;
    }

    public void setFlowType(PaymentFlowType flowType) {
        this.flowType = flowType;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getPriorityOrder() {
        return priorityOrder;
    }

    public void setPriorityOrder(int priorityOrder) {
        this.priorityOrder = priorityOrder;
    }

    public String getSupportedCurrenciesJson() {
        return supportedCurrenciesJson;
    }

    public void setSupportedCurrenciesJson(String supportedCurrenciesJson) {
        this.supportedCurrenciesJson = supportedCurrenciesJson;
    }

    public String getConfigurationJson() {
        return configurationJson;
    }

    public void setConfigurationJson(String configurationJson) {
        this.configurationJson = configurationJson;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
