package com.cyancoder.aiorchestrator.domain;

import java.util.LinkedHashMap;
import java.util.Map;

public class EntityBlueprint {
    private String serviceKey;
    private String templateKey;
    private String entityKey;
    private String recordKey;
    private boolean createDefinition = true;
    private boolean createRecord;
    private Map<String, Object> recordData = new LinkedHashMap<>();

    public String getServiceKey() { return serviceKey; }
    public void setServiceKey(String serviceKey) { this.serviceKey = serviceKey; }
    public String getTemplateKey() { return templateKey; }
    public void setTemplateKey(String templateKey) { this.templateKey = templateKey; }
    public String getEntityKey() { return entityKey; }
    public void setEntityKey(String entityKey) { this.entityKey = entityKey; }
    public String getRecordKey() { return recordKey; }
    public void setRecordKey(String recordKey) { this.recordKey = recordKey; }
    public boolean isCreateDefinition() { return createDefinition; }
    public void setCreateDefinition(boolean createDefinition) { this.createDefinition = createDefinition; }
    public boolean isCreateRecord() { return createRecord; }
    public void setCreateRecord(boolean createRecord) { this.createRecord = createRecord; }
    public Map<String, Object> getRecordData() { return recordData; }
    public void setRecordData(Map<String, Object> recordData) { this.recordData = recordData; }
}

