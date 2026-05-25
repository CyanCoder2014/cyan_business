package com.cyancoder.bpm.domain;

public class ManagedObjectRef {
    private String service;
    private String entityKey;
    private String recordKey;

    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
    public String getEntityKey() { return entityKey; }
    public void setEntityKey(String entityKey) { this.entityKey = entityKey; }
    public String getRecordKey() { return recordKey; }
    public void setRecordKey(String recordKey) { this.recordKey = recordKey; }
}

