package com.cyancoder.storefront.model;

import java.util.Map;

public class ResolvedRouteResponse {
    private String tenantKey;
    private String siteKey;
    private String path;
    private Map<String, Object> route;
    private Map<String, Object> target;
    private Map<String, Object> theme;

    public String getTenantKey() { return tenantKey; }
    public void setTenantKey(String tenantKey) { this.tenantKey = tenantKey; }
    public String getSiteKey() { return siteKey; }
    public void setSiteKey(String siteKey) { this.siteKey = siteKey; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public Map<String, Object> getRoute() { return route; }
    public void setRoute(Map<String, Object> route) { this.route = route; }
    public Map<String, Object> getTarget() { return target; }
    public void setTarget(Map<String, Object> target) { this.target = target; }
    public Map<String, Object> getTheme() { return theme; }
    public void setTheme(Map<String, Object> theme) { this.theme = theme; }
}
