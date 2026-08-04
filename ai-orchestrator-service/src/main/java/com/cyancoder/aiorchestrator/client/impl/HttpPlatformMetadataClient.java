package com.cyancoder.aiorchestrator.client.impl;

import com.cyancoder.aiorchestrator.client.PlatformMetadataClient;
import com.cyancoder.aiorchestrator.config.PlatformMetadataProperties;
import com.cyancoder.aiorchestrator.service.ServiceAvailabilitySnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class HttpPlatformMetadataClient implements PlatformMetadataClient {
    private static final Set<String> TEMPLATE_SERVICES = Set.of(
            "content-service", "catalog-service", "crm-service", "commerce-service",
            "finance-service", "inventory-service", "report-service", "storefront-service",
            "media-service", "cart-service", "checkout-service", "pricing-promotion-service",
            "search-index-service", "notification-service", "bpm-service"
    );
    private static final Set<String> HTTP_METHODS = Set.of(
            "get", "post", "put", "patch", "delete", "head", "options", "trace");
    private final InternalServiceHttpSupport httpSupport;
    private final PlatformMetadataProperties properties;
    private final ObjectMapper objectMapper;

    public HttpPlatformMetadataClient(InternalServiceHttpSupport httpSupport,
                                      PlatformMetadataProperties properties,
                                      ObjectMapper objectMapper) {
        this.httpSupport = httpSupport;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> fetchMetadata(String tenantKey, String siteKey,
                                             ServiceAvailabilitySnapshot availability) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("_serviceAvailability", Map.of(
                "source", availability.source(),
                "availableServiceKeys", availability.availableServiceKeys()
        ));
        boolean apiCatalogAvailable = availability.availableServiceKeys()
                .contains("api-docs-service");
        for (String serviceKey : availability.availableServiceKeys()) {
            Map<String, Object> serviceMetadata = new LinkedHashMap<>();
            serviceMetadata.put("status", "AVAILABLE");
            if (TEMPLATE_SERVICES.contains(serviceKey)) {
                serviceMetadata.put("templates", fetchBody(serviceKey, "/internal/entities/templates", tenantKey, siteKey, "[]"));
            }
            if ("bpm-service".equals(serviceKey)) {
                serviceMetadata.put("actions", fetchBody(serviceKey, "/internal/bpm/metadata/actions", tenantKey, siteKey, "[]"));
                serviceMetadata.put("transitionConditions", fetchBody(serviceKey, "/internal/bpm/metadata/transition-conditions", tenantKey, siteKey, "{}"));
            }
            if (apiCatalogAvailable && !"api-docs-service".equals(serviceKey)) {
                serviceMetadata.put("controllerApis", fetchControllerApis(
                        serviceKey, tenantKey, siteKey));
            }
            metadata.put(serviceKey, serviceMetadata);
        }
        return metadata;
    }

    private List<Map<String, Object>> fetchControllerApis(
            String serviceKey,
            String tenantKey,
            String siteKey
    ) {
        try {
            String body = httpSupport.get(
                    "api-docs-service",
                    "/internal/api-docs/services/" + UriUtils.encodePathSegment(
                            serviceKey, StandardCharsets.UTF_8),
                    tenantKey,
                    siteKey);
            JsonNode paths = objectMapper.readTree(body).path("paths");
            if (!paths.isObject()) {
                return List.of();
            }
            List<Map<String, Object>> operations = new ArrayList<>();
            paths.properties().forEach(pathEntry ->
                    pathEntry.getValue().properties().forEach(operationEntry -> {
                        String method = operationEntry.getKey().toLowerCase(Locale.ROOT);
                        if (!HTTP_METHODS.contains(method) || operations.size() >= 500) {
                            return;
                        }
                        JsonNode operation = operationEntry.getValue();
                        Map<String, Object> summary = new LinkedHashMap<>();
                        summary.put("method", method.toUpperCase(Locale.ROOT));
                        summary.put("path", pathEntry.getKey());
                        summary.put("operationId", operation.path("operationId").asText(""));
                        summary.put("summary", operation.path("summary").asText(""));
                        summary.put("auth", operation.path("x-platform-auth").asText("BEARER"));
                        operations.add(summary);
                    }));
            return operations;
        } catch (Exception exception) {
            return List.of();
        }
    }

    private Object fetchBody(String serviceKey, String path, String tenantKey, String siteKey, String defaultJson) {
        try {
            String body = httpSupport.get(serviceKey, path, tenantKey, siteKey);
            return objectMapper.readValue(body == null || body.isBlank() ? defaultJson : body, Object.class);
        } catch (Exception ex) {
            return defaultJson.startsWith("{") ? Map.of() : java.util.List.of();
        }
    }
}
