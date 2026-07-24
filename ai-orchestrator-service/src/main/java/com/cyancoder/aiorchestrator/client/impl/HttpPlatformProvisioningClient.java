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

    @Override
    public Map<String, Object> upsertResource(String resourceType, String serviceKey, String resourceKey,
                                              Map<String, Object> body, String tenantKey, String siteKey) {
        try {
            String response = switch (resourceType) {
                case "PROCESSOR_DEFINITION" -> upsertProcessor(resourceKey, body, tenantKey, siteKey);
                case "AUTOMATION_FLOW" -> upsertAutomation(resourceKey, body, tenantKey, siteKey);
                case "BATCH_DEFINITION" -> httpSupport.post(
                        "batch-worker-service", "/internal/batch/definitions", body, tenantKey, siteKey);
                default -> throw new IllegalArgumentException("Unsupported resourceType: " + resourceType);
            };
            return objectMapper.readValue(response, Map.class);
        } catch (DownstreamServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to upsert " + resourceType + " " + resourceKey, ex);
        }
    }

    private String upsertProcessor(String resourceKey, Map<String, Object> body,
                                   String tenantKey, String siteKey) {
        try {
            return httpSupport.post("processor-service", "/api/processor-service/processors",
                    body, tenantKey, siteKey);
        } catch (DownstreamServiceException ex) {
            if (ex.getDownstreamStatus() != null && ex.getDownstreamStatus() >= 409) {
                return httpSupport.put("processor-service",
                        "/api/processor-service/processors/" + resourceKey,
                        body, tenantKey, siteKey);
            }
            throw ex;
        }
    }

    private String upsertAutomation(String resourceKey, Map<String, Object> body,
                                    String tenantKey, String siteKey) {
        try {
            return httpSupport.post("automation-orchestrator-service",
                    "/internal/automation-flows", body, tenantKey, siteKey);
        } catch (DownstreamServiceException ex) {
            if (ex.getDownstreamStatus() != null && ex.getDownstreamStatus() >= 409) {
                int version = body.get("version") instanceof Number value ? value.intValue() : 1;
                String existingJson = httpSupport.get("automation-orchestrator-service",
                        "/internal/automation-flows/" + resourceKey + "/versions/" + version,
                        tenantKey, siteKey);
                try {
                    Map<String, Object> existing = objectMapper.readValue(existingJson, Map.class);
                    Map<String, Object> replacement = new LinkedHashMap<>(body);
                    replacement.put("id", existing.get("id"));
                    replacement.put("revision", existing.get("revision"));
                    return httpSupport.post("automation-orchestrator-service",
                            "/internal/automation-flows", replacement, tenantKey, siteKey);
                } catch (Exception parseException) {
                    throw new IllegalStateException(
                            "Failed to update automation flow " + resourceKey + " version " + version,
                            parseException);
                }
            }
            throw ex;
        }
    }
}
