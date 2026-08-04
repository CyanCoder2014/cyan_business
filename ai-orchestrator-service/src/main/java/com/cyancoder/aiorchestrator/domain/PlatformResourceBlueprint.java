package com.cyancoder.aiorchestrator.domain;

import java.util.LinkedHashMap;
import java.util.Map;

public class PlatformResourceBlueprint {
    private String resourceType;
    private String serviceKey;
    private String resourceKey;
    private Map<String, Object> body = new LinkedHashMap<>();

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public String getServiceKey() { return serviceKey; }
    public void setServiceKey(String serviceKey) { this.serviceKey = serviceKey; }
    public String getResourceKey() { return resourceKey; }
    public void setResourceKey(String resourceKey) { this.resourceKey = resourceKey; }
    public Map<String, Object> getBody() { return body; }
    public void setBody(Map<String, Object> body) {
        this.body = body == null ? new LinkedHashMap<>() : new LinkedHashMap<>(body);
    }
}
