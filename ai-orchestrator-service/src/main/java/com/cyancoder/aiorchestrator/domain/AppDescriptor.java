package com.cyancoder.aiorchestrator.domain;

import java.util.ArrayList;
import java.util.List;

public class AppDescriptor {
    private String appKey;
    private String title;
    private PlatformAppType type = PlatformAppType.MIXED_BUSINESS_APP;
    private String tenantKey;
    private String siteKey;
    private String desiredDomain;
    private List<String> capabilities = new ArrayList<>();
    private List<String> availableServiceKeys = new ArrayList<>();

    public String getAppKey() { return appKey; }
    public void setAppKey(String appKey) { this.appKey = appKey; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public PlatformAppType getType() { return type; }
    public void setType(PlatformAppType type) { this.type = type; }
    public String getTenantKey() { return tenantKey; }
    public void setTenantKey(String tenantKey) { this.tenantKey = tenantKey; }
    public String getSiteKey() { return siteKey; }
    public void setSiteKey(String siteKey) { this.siteKey = siteKey; }
    public String getDesiredDomain() { return desiredDomain; }
    public void setDesiredDomain(String desiredDomain) { this.desiredDomain = desiredDomain; }
    public List<String> getCapabilities() { return capabilities; }
    public void setCapabilities(List<String> capabilities) { this.capabilities = capabilities; }
    public List<String> getAvailableServiceKeys() { return availableServiceKeys; }
    public void setAvailableServiceKeys(List<String> availableServiceKeys) {
        this.availableServiceKeys = availableServiceKeys == null ? new ArrayList<>() : new ArrayList<>(availableServiceKeys);
    }
}
