package com.cyancoder.dynamiccore.runtime;

import java.util.Map;

public class DynamicRecordRequest {
    private String recordKey;
    private Map<String, Object> data;

    public String getRecordKey() { return recordKey; }
    public void setRecordKey(String recordKey) { this.recordKey = recordKey; }
    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
}
