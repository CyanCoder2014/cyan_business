package com.cyancoder.apidocs.service;

import com.cyancoder.apidocs.config.ApiDocsCatalogProperties;
import com.cyancoder.apidocs.model.ApiDocsTarget;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ApiDocsTargetRegistry {
    private final Map<String, ApiDocsTarget> targets;

    public ApiDocsTargetRegistry(ApiDocsCatalogProperties properties, ObjectMapper objectMapper) {
        try {
            List<ApiDocsTarget> configured = objectMapper.readValue(
                    properties.getTargetsJson(),
                    new TypeReference<>() {
                    });
            Map<String, ApiDocsTarget> resolved = new LinkedHashMap<>();
            for (ApiDocsTarget target : configured) {
                if (target.serviceKey() == null || target.serviceKey().isBlank()) {
                    throw new IllegalArgumentException("API docs target serviceKey is required");
                }
                if (target.baseUrl() == null || target.baseUrl().isBlank()) {
                    throw new IllegalArgumentException(
                            "API docs target baseUrl is required: " + target.serviceKey());
                }
                if (target.isEnabled()) {
                    resolved.put(target.serviceKey(), target);
                }
            }
            this.targets = Collections.unmodifiableMap(new LinkedHashMap<>(resolved));
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid platform.api-docs.targets-json", exception);
        }
    }

    public List<ApiDocsTarget> list() {
        return targets.values().stream().toList();
    }

    public ApiDocsTarget get(String serviceKey) {
        ApiDocsTarget target = targets.get(serviceKey);
        if (target == null) {
            throw new IllegalArgumentException("API docs target not found: " + serviceKey);
        }
        return target;
    }
}
