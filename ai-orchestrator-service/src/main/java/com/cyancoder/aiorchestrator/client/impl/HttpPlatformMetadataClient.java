package com.cyancoder.aiorchestrator.client.impl;

import com.cyancoder.aiorchestrator.client.PlatformMetadataClient;
import com.cyancoder.aiorchestrator.config.PlatformMetadataProperties;
import com.cyancoder.aiorchestrator.service.ServiceAvailabilitySnapshot;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
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
            metadata.put(serviceKey, serviceMetadata);
        }
        return metadata;
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
