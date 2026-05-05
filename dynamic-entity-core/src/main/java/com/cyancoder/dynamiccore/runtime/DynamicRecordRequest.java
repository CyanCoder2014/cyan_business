package com.cyancoder.dynamiccore.runtime;

import java.util.Map;

public class DynamicRecordRequest {
    private String recordKey;
    private String tenantKey;
    private String siteKey;
    private Map<String, Object> data;

    public String getRecordKey() { return recordKey; }
    public void setRecordKey(String recordKey) { this.recordKey = recordKey; }
    public String getTenantKey() { return tenantKey; }
    public void setTenantKey(String tenantKey) { this.tenantKey = tenantKey; }
    public String getSiteKey() { return siteKey; }
    public void setSiteKey(String siteKey) { this.siteKey = siteKey; }
    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
}
