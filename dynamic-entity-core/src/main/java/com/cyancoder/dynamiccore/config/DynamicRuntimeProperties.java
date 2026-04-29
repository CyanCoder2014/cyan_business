package com.cyancoder.dynamiccore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dynamic.runtime")
public class DynamicRuntimeProperties {
    private String serviceKey = "dynamic-service";
    private boolean checkMissingFields = true;
    private boolean checkExtraFields = true;

    public String getServiceKey() { return serviceKey; }
    public void setServiceKey(String serviceKey) { this.serviceKey = serviceKey; }
    public boolean isCheckMissingFields() { return checkMissingFields; }
    public void setCheckMissingFields(boolean checkMissingFields) { this.checkMissingFields = checkMissingFields; }
    public boolean isCheckExtraFields() { return checkExtraFields; }
    public void setCheckExtraFields(boolean checkExtraFields) { this.checkExtraFields = checkExtraFields; }
}
