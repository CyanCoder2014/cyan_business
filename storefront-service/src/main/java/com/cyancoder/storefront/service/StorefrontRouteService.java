package com.cyancoder.storefront.service;

import com.cyancoder.dynamiccore.runtime.DynamicRuntimeService;
import com.cyancoder.dynamiccore.runtime.DynamicScope;
import com.cyancoder.dynamiccore.store.mongo.DynamicEntityRecordDocument;
import com.cyancoder.storefront.model.ResolvedRouteResponse;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class StorefrontRouteService {
    private final DynamicRuntimeService dynamicRuntimeService;
    private final InternalServiceHttpSupport internalServiceHttpSupport;

    public StorefrontRouteService(DynamicRuntimeService dynamicRuntimeService, InternalServiceHttpSupport internalServiceHttpSupport) {
        this.dynamicRuntimeService = dynamicRuntimeService;
        this.internalServiceHttpSupport = internalServiceHttpSupport;
    }

    public ResolvedRouteResponse resolve(String path, DynamicScope scope) {
        String normalizedPath = normalizePath(path);
        DynamicEntityRecordDocument route = dynamicRuntimeService.listRecords("site-route", scope).stream()
                .filter(record -> "ACTIVE".equalsIgnoreCase(record.getStatus()))
                .filter(record -> "PUBLISHED".equalsIgnoreCase(Objects.toString(recordData(record).get("publicationStatus"), "")))
                .filter(record -> normalizedPath.equals(Objects.toString(recordData(record).get("path"), "")))
                .findFirst()
                .orElseThrow();

        Map<String, Object> routeData = new LinkedHashMap<>(recordData(route));
        Map<String, Object> target = resolveTarget(routeData, scope);
        Map<String, Object> theme = resolveTheme(routeData, scope);

        ResolvedRouteResponse response = new ResolvedRouteResponse();
        response.setTenantKey(scope.tenantKey());
        response.setSiteKey(scope.siteKey());
        response.setPath(normalizedPath);
        response.setRoute(routeData);
        response.setTarget(target);
        response.setTheme(theme);
        return response;
    }

    public List<Map<String, Object>> sitemap(DynamicScope scope) {
        return dynamicRuntimeService.listRecords("site-route", scope).stream()
                .map(DynamicEntityRecordDocument::getData)
                .filter(Objects::nonNull)
                .filter(route -> "PUBLISHED".equalsIgnoreCase(Objects.toString(route.get("publicationStatus"), "")))
                .filter(route -> "true".equalsIgnoreCase(Objects.toString(route.get("indexingEnabled"), "true")))
                .map(route -> Map.of(
                        "path", Objects.toString(route.get("path"), ""),
                        "routeType", Objects.toString(route.get("routeType"), ""),
                        "sitemapPriority", Objects.toString(route.get("sitemapPriority"), "0.8"),
                        "canonicalUrl", Objects.toString(nestedValue(route, "seo", "canonicalUrl"), ""),
                        "lastModified", ""
                ))
                .toList();
    }

    private Map<String, Object> resolveTarget(Map<String, Object> routeData, DynamicScope scope) {
        Object raw = routeData.get("entityRef");
        if (!(raw instanceof Map<?, ?> entityRef)) {
            return Map.of();
        }
        String service = Objects.toString(entityRef.get("service"), "");
        String entityKey = Objects.toString(entityRef.get("entityKey"), "");
        String recordKey = Objects.toString(entityRef.get("recordKey"), "");
        if (service.isBlank() || entityKey.isBlank() || recordKey.isBlank()) {
            return Map.of();
        }
        Map<String, Object> record = internalServiceHttpSupport.get(service,
                "/internal/entities/records/" + entityKey + "/" + recordKey,
                scope.tenantKey(),
                scope.siteKey(),
                Map.class);
        return record == null ? Map.of() : record;
    }

    private Map<String, Object> resolveTheme(Map<String, Object> routeData, DynamicScope scope) {
        Object rendering = routeData.get("rendering");
        if (!(rendering instanceof Map<?, ?> map)) {
            return Map.of();
        }
        String themeKey = Objects.toString(map.get("themeKey"), "");
        if (themeKey.isBlank()) {
            return Map.of();
        }
        DynamicEntityRecordDocument theme = dynamicRuntimeService.getRecord("theme-layout", themeKey, scope);
        return theme.getData() == null ? Map.of() : theme.getData();
    }

    @SuppressWarnings("unchecked")
    private Object nestedValue(Map<String, Object> data, String objectKey, String fieldKey) {
        Object nested = data.get(objectKey);
        if (nested instanceof Map<?, ?> map) {
            return ((Map<String, Object>) map).get(fieldKey);
        }
        return null;
    }

    private Map<String, Object> recordData(DynamicEntityRecordDocument record) {
        return record.getData() == null ? Map.of() : record.getData();
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) {
            return "/";
        }
        String normalized = path.startsWith("/") ? path : "/" + path;
        return normalized.endsWith("/") && normalized.length() > 1
                ? normalized.substring(0, normalized.length() - 1)
                : normalized;
    }
}
