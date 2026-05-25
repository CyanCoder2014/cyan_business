package com.cyancoder.aiorchestrator.client.impl;

import com.cyancoder.aiorchestrator.client.PlatformProvisioningClient;
import com.cyancoder.aiorchestrator.exception.DownstreamServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class HttpPlatformProvisioningClient implements PlatformProvisioningClient {
    private final InternalServiceHttpSupport httpSupport;
    private final ObjectMapper objectMapper;

    public HttpPlatformProvisioningClient(InternalServiceHttpSupport httpSupport, ObjectMapper objectMapper) {
        this.httpSupport = httpSupport;
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> createDefinitionFromTemplate(String serviceKey, String templateKey, String entityKey, String tenantKey, String siteKey) {
        try {
            Map<String, Object> request = new LinkedHashMap<>();
            request.put("entityKey", entityKey);
            request.put("tenantKey", tenantKey);
            request.put("siteKey", siteKey);
            String body = httpSupport.post(serviceKey,
                    "/internal/entities/templates/" + templateKey + "/definitions",
                    request,
                    tenantKey,
                    siteKey);
            return objectMapper.readValue(body, Map.class);
        } catch (DownstreamServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to create definition from template " + templateKey + " in " + serviceKey, ex);
        }
    }

    @Override
    public Map<String, Object> createRecord(String serviceKey, String entityKey, String recordKey, Map<String, Object> data, String tenantKey, String siteKey) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("recordKey", recordKey);
            body.put("tenantKey", tenantKey);
            body.put("siteKey", siteKey);
            body.put("data", data);
            String response = httpSupport.post(serviceKey, "/internal/entities/records/" + entityKey, body, tenantKey, siteKey);
            return objectMapper.readValue(response, Map.class);
        } catch (DownstreamServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to create record " + recordKey + " in " + serviceKey + "/" + entityKey, ex);
        }
    }

    @Override
    public Map<String, Object> createBpmFlow(Map<String, Object> flowDefinition, String tenantKey, String siteKey) {
        try {
            String response = httpSupport.post("bpm-service", "/internal/bpm/flows", flowDefinition, tenantKey, siteKey);
            return objectMapper.readValue(response, Map.class);
        } catch (DownstreamServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to create BPM flow", ex);
        }
    }
}
