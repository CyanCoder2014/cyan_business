package com.cyancoder.aiorchestrator.client;

import java.util.Map;

public interface PlatformProvisioningClient {
    Map<String, Object> createDefinitionFromTemplate(String serviceKey, String templateKey, String entityKey, String tenantKey, String siteKey);
    Map<String, Object> createRecord(String serviceKey, String entityKey, String recordKey, Map<String, Object> data, String tenantKey, String siteKey);
    Map<String, Object> createBpmFlow(Map<String, Object> flowDefinition, String tenantKey, String siteKey);
    Map<String, Object> upsertResource(String resourceType, String serviceKey, String resourceKey,
                                       Map<String, Object> body, String tenantKey, String siteKey);
}
