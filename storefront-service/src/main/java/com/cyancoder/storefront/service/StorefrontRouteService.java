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

    public Map<String, Object> render(String path, DynamicScope scope) {
        ResolvedRouteResponse resolved = resolve(path, scope);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("tenantKey", resolved.getTenantKey());
        response.put("siteKey", resolved.getSiteKey());
        response.put("path", resolved.getPath());
        response.put("route", resolved.getRoute());
        response.put("target", resolved.getTarget());
        response.put("theme", resolved.getTheme());
        response.put("html", renderHtml(resolved));
        return response;
    }

    public String renderHtml(String path, DynamicScope scope) {
        return renderHtml(resolve(path, scope));
    }

    public List<Map<String, Object>> sitemap(DynamicScope scope) {
        return dynamicRuntimeService.listRecords("site-route", scope).stream()
                .map(DynamicEntityRecordDocument::getData)
                .filter(Objects::nonNull)
                .filter(route -> "PUBLISHED".equalsIgnoreCase(Objects.toString(route.get("publicationStatus"), "")))
                .filter(route -> "true".equalsIgnoreCase(Objects.toString(route.get("indexingEnabled"), "true")))
                .map(route -> {
                    Map<String, Object> sitemapRow = new LinkedHashMap<>();
                    sitemapRow.put("path", Objects.toString(route.get("path"), ""));
                    sitemapRow.put("routeType", Objects.toString(route.get("routeType"), ""));
                    sitemapRow.put("sitemapPriority", Objects.toString(route.get("sitemapPriority"), "0.8"));
                    sitemapRow.put("canonicalUrl", Objects.toString(nestedValue(route, "seo", "canonicalUrl"), ""));
                    sitemapRow.put("lastModified", "");
                    return sitemapRow;
                })
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
        try {
            DynamicEntityRecordDocument theme = dynamicRuntimeService.getRecord("theme-layout", themeKey, scope);
            return theme.getData() == null ? Map.of() : theme.getData();
        } catch (Exception ex) {
            return Map.of();
        }
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

    @SuppressWarnings("unchecked")
    private String renderHtml(ResolvedRouteResponse resolved) {
        Map<String, Object> route = resolved.getRoute() == null ? Map.of() : resolved.getRoute();
        Map<String, Object> targetRecord = resolved.getTarget() == null ? Map.of() : resolved.getTarget();
        Map<String, Object> targetData = targetRecord.get("data") instanceof Map<?, ?> data ? (Map<String, Object>) data : Map.of();
        Map<String, Object> theme = resolved.getTheme() == null ? Map.of() : resolved.getTheme();
        Map<String, Object> seo = route.get("seo") instanceof Map<?, ?> data ? (Map<String, Object>) data : Map.of();
        String title = firstNonBlank(seo.get("title"), targetData.get("title"), targetData.get("name"), Objects.toString(route.get("routeKey"), "Storefront"));
        String description = firstNonBlank(seo.get("description"), targetData.get("summary"), targetData.get("description"), "");
        String brandName = Objects.toString(theme.get("brandName"), "Dynamic Storefront");
        String bodyTitle = firstNonBlank(targetData.get("title"), targetData.get("name"), title);
        String bodyContent = firstNonBlank(targetData.get("body"), targetData.get("content"), targetData.get("summary"), targetData.get("description"), "");

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\">");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
        html.append("<title>").append(escapeHtml(title)).append("</title>");
        if (!description.isBlank()) {
            html.append("<meta name=\"description\" content=\"").append(escapeHtml(description)).append("\">");
        }
        html.append("<meta property=\"og:site_name\" content=\"").append(escapeHtml(brandName)).append("\">");
        html.append("</head><body>");
        html.append("<header><h1>").append(escapeHtml(brandName)).append("</h1></header>");
        html.append("<main>");
        html.append("<article>");
        html.append("<h2>").append(escapeHtml(bodyTitle)).append("</h2>");
        if (!description.isBlank()) {
            html.append("<p>").append(escapeHtml(description)).append("</p>");
        }
        if (!bodyContent.isBlank()) {
            html.append("<section>").append(escapeHtml(bodyContent)).append("</section>");
        }
        html.append("</article>");
        html.append("</main>");
        html.append("</body></html>");
        return html.toString();
    }

    private String firstNonBlank(Object... values) {
        for (Object value : values) {
            if (value != null) {
                String string = String.valueOf(value);
                if (!string.isBlank()) {
                    return string;
                }
            }
        }
        return "";
    }

    private String escapeHtml(String input) {
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
