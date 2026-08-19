package com.cyancoder.storefront.service;

import com.cyancoder.dynamiccore.runtime.DynamicRuntimeService;
import com.cyancoder.dynamiccore.runtime.DynamicScope;
import com.cyancoder.dynamiccore.store.mongo.DynamicEntityRecordDocument;
import com.cyancoder.storefront.model.ResolvedRouteResponse;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class StorefrontRouteService {
    private final DynamicRuntimeService dynamicRuntimeService;
    private final InternalServiceHttpSupport internalServiceHttpSupport;

    public StorefrontRouteService(DynamicRuntimeService dynamicRuntimeService, InternalServiceHttpSupport internalServiceHttpSupport) {
        this.dynamicRuntimeService = dynamicRuntimeService;
        this.internalServiceHttpSupport = internalServiceHttpSupport;
    }

    public ResolvedRouteResponse resolve(String path, DynamicScope scope) {
        return resolve(path, null, scope);
    }

    public ResolvedRouteResponse resolve(String path, String host, DynamicScope scope) {
        String normalizedPath = normalizePath(path);
        Map<String, Object> binding = resolveDomainBinding(host, normalizedPath, scope);
        String resolvedPath = firstNonBlank(binding.get("targetPath"), normalizedPath);
        String routeKey = Objects.toString(binding.get("routeKey"), "");
        DynamicEntityRecordDocument route = dynamicRuntimeService.listRecords("site-route", scope).stream()
                .filter(record -> "ACTIVE".equalsIgnoreCase(record.getStatus()))
                .filter(record -> "PUBLISHED".equalsIgnoreCase(Objects.toString(recordData(record).get("publicationStatus"), "")))
                .filter(record -> routeKey.isBlank() || routeKey.equals(Objects.toString(recordData(record).get("routeKey"), "")))
                .filter(record -> resolvedPath.equals(Objects.toString(recordData(record).get("path"), "")))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Published site route was not found"
                ));

        Map<String, Object> routeData = new LinkedHashMap<>(recordData(route));
        if (!binding.isEmpty()) {
            routeData.put("domainBinding", binding);
        }
        Map<String, Object> target = resolveTarget(routeData, scope);
        Map<String, Object> theme = resolveTheme(routeData, scope);

        ResolvedRouteResponse response = new ResolvedRouteResponse();
        response.setTenantKey(scope.tenantKey());
        response.setSiteKey(scope.siteKey());
        response.setPath(resolvedPath);
        response.setRoute(routeData);
        response.setTarget(target);
        response.setTheme(theme);
        return response;
    }

    public Map<String, Object> render(String path, DynamicScope scope) {
        return render(path, null, scope);
    }

    public Map<String, Object> render(String path, String host, DynamicScope scope) {
        ResolvedRouteResponse resolved = resolve(path, host, scope);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("tenantKey", resolved.getTenantKey());
        response.put("siteKey", resolved.getSiteKey());
        response.put("path", resolved.getPath());
        response.put("route", resolved.getRoute());
        response.put("target", resolved.getTarget());
        response.put("theme", resolved.getTheme());
        response.put("html", renderHtml(resolved));
        response.put("resolvedHost", host);
        return response;
    }

    public String renderHtml(String path, DynamicScope scope) {
        return renderHtml(path, null, scope);
    }

    public String renderHtml(String path, String host, DynamicScope scope) {
        return renderHtml(resolve(path, host, scope));
    }

    public List<Map<String, Object>> sitemap(DynamicScope scope) {
        return sitemap(null, scope);
    }

    public List<Map<String, Object>> sitemap(String host, DynamicScope scope) {
        return dynamicRuntimeService.listRecords("site-route", scope).stream()
                .filter(record -> record.getData() != null)
                .filter(record -> "PUBLISHED".equalsIgnoreCase(Objects.toString(record.getData().get("publicationStatus"), "")))
                .filter(record -> "true".equalsIgnoreCase(Objects.toString(record.getData().get("indexingEnabled"), "true")))
                .map(record -> {
                    Map<String, Object> route = record.getData();
                    Map<String, Object> binding = resolveDomainBinding(host, Objects.toString(route.get("path"), "/"), scope);
                    Map<String, Object> sitemapRow = new LinkedHashMap<>();
                    sitemapRow.put("path", Objects.toString(route.get("path"), ""));
                    sitemapRow.put("routeType", Objects.toString(route.get("routeType"), ""));
                    sitemapRow.put("sitemapPriority", Objects.toString(route.get("sitemapPriority"), "0.8"));
                    sitemapRow.put("canonicalUrl", firstNonBlank(nestedValue(route, "seo", "canonicalUrl"), absoluteUrl(route, binding, host)));
                    sitemapRow.put("lastModified", Objects.toString(record.getUpdatedAt(), ""));
                    sitemapRow.put("changeFrequency", Objects.toString(nestedValue(route, "seo", "changeFrequency"), "weekly"));
                    return sitemapRow;
                })
                .toList();
    }

    public String sitemapXml(DynamicScope scope) {
        return sitemapXml(null, scope);
    }

    public String sitemapXml(String host, DynamicScope scope) {
        List<Map<String, Object>> routes = sitemap(host, scope);
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
        for (Map<String, Object> route : routes) {
            xml.append("<url>");
            xml.append("<loc>").append(escapeXml(absoluteUrl(route, Map.of(), null))).append("</loc>");
            String lastModified = Objects.toString(route.get("lastModified"), "");
            if (!lastModified.isBlank()) {
                xml.append("<lastmod>").append(escapeXml(lastModified)).append("</lastmod>");
            }
            xml.append("<changefreq>").append(escapeXml(defaulted(route.get("changeFrequency"), "weekly"))).append("</changefreq>");
            xml.append("<priority>").append(escapeXml(defaulted(route.get("sitemapPriority"), "0.8"))).append("</priority>");
            xml.append("</url>");
        }
        xml.append("</urlset>");
        return xml.toString();
    }

    public String robotsTxt(DynamicScope scope) {
        return robotsTxt(null, scope);
    }

    public String robotsTxt(String host, DynamicScope scope) {
        String sitemapUrl = sitemap(host, scope).stream()
                .findFirst()
                .map(route -> absoluteUrl(route, Map.of(), host))
                .map(url -> url.endsWith("/") ? url + "public/storefront/sitemap.xml" : url.substring(0, url.lastIndexOf('/')) + "/public/storefront/sitemap.xml")
                .orElse("/public/storefront/sitemap.xml");
        return "User-agent: *\nAllow: /\nSitemap: " + sitemapUrl + "\n";
    }

    public String resolveHost(String host, String forwardedHost) {
        return normalizeHost(firstNonBlank(forwardedHost, host));
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
        Map<String, Object> domainBinding = route.get("domainBinding") instanceof Map<?, ?> data ? (Map<String, Object>) data : Map.of();
        String title = firstNonBlank(seo.get("title"), targetData.get("title"), targetData.get("name"), Objects.toString(route.get("routeKey"), "Storefront"));
        String description = firstNonBlank(seo.get("description"), targetData.get("summary"), targetData.get("description"), "");
        String brandName = Objects.toString(theme.get("brandName"), "Dynamic Storefront");
        String bodyTitle = firstNonBlank(targetData.get("title"), targetData.get("name"), title);
        String bodyContent = firstNonBlank(targetData.get("body"), targetData.get("content"), targetData.get("summary"), targetData.get("description"), "");
        String canonicalUrl = firstNonBlank(
                seo.get("canonicalUrl"),
                absoluteUrl(route, domainBinding, null),
                nestedValue(targetData, "seo", "canonicalUrl"),
                nestedValue(targetData, "routing", "primaryPath"),
                resolved.getPath()
        );
        String robots = firstNonBlank(seo.get("robots"), "index,follow");
        String ogImage = firstNonBlank(seo.get("ogImage"), nestedMediaUrl(targetData), "");
        String twitterCard = firstNonBlank(seo.get("twitterCard"), "summary_large_image");
        String schemaJson = buildSchemaJson(route, targetData, brandName, title, description, canonicalUrl);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\">");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
        html.append("<title>").append(escapeHtml(title)).append("</title>");
        html.append("<link rel=\"canonical\" href=\"").append(escapeHtml(canonicalUrl)).append("\">");
        html.append("<meta name=\"robots\" content=\"").append(escapeHtml(robots)).append("\">");
        if (!description.isBlank()) {
            html.append("<meta name=\"description\" content=\"").append(escapeHtml(description)).append("\">");
        }
        html.append("<meta property=\"og:title\" content=\"").append(escapeHtml(title)).append("\">");
        html.append("<meta property=\"og:description\" content=\"").append(escapeHtml(description)).append("\">");
        html.append("<meta property=\"og:site_name\" content=\"").append(escapeHtml(brandName)).append("\">");
        html.append("<meta property=\"og:type\" content=\"").append(escapeHtml(ogType(route))).append("\">");
        html.append("<meta property=\"og:url\" content=\"").append(escapeHtml(canonicalUrl)).append("\">");
        if (!ogImage.isBlank()) {
            html.append("<meta property=\"og:image\" content=\"").append(escapeHtml(ogImage)).append("\">");
        }
        html.append("<meta name=\"twitter:card\" content=\"").append(escapeHtml(twitterCard)).append("\">");
        html.append("<meta name=\"twitter:title\" content=\"").append(escapeHtml(title)).append("\">");
        html.append("<meta name=\"twitter:description\" content=\"").append(escapeHtml(description)).append("\">");
        if (!ogImage.isBlank()) {
            html.append("<meta name=\"twitter:image\" content=\"").append(escapeHtml(ogImage)).append("\">");
        }
        html.append("<script type=\"application/ld+json\">").append(escapeHtml(schemaJson)).append("</script>");
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

    @SuppressWarnings("unchecked")
    private String nestedMediaUrl(Map<String, Object> targetData) {
        Object media = targetData.get("media");
        if (!(media instanceof List<?> list) || list.isEmpty() || !(list.get(0) instanceof Map<?, ?> map)) {
            return "";
        }
        return Objects.toString(((Map<String, Object>) map).get("url"), "");
    }

    private String ogType(Map<String, Object> route) {
        String routeType = Objects.toString(route.get("routeType"), "");
        if ("PRODUCT".equalsIgnoreCase(routeType)) {
            return "product";
        }
        if ("BLOG".equalsIgnoreCase(routeType) || "ARTICLE".equalsIgnoreCase(routeType)) {
            return "article";
        }
        return "website";
    }

    @SuppressWarnings("unchecked")
    private String buildSchemaJson(Map<String, Object> route, Map<String, Object> targetData, String brandName, String title, String description, String canonicalUrl) {
        Object explicitBlocks = nestedValue(route, "seo", "structuredDataBlocks");
        if (explicitBlocks instanceof List<?> blocks && !blocks.isEmpty()) {
            return blocks.stream().map(String::valueOf).collect(Collectors.joining(", ", "[", "]"));
        }
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("@context", "https://schema.org");
        String routeType = Objects.toString(route.get("routeType"), "");
        if ("PRODUCT".equalsIgnoreCase(routeType)) {
            schema.put("@type", "Product");
            schema.put("name", firstNonBlank(targetData.get("name"), title));
            schema.put("description", description);
            schema.put("url", canonicalUrl);
            schema.put("brand", Map.of("@type", "Brand", "name", brandName));
            Object price = targetData.get("defaultPrice");
            if (price != null) {
                schema.put("offers", Map.of(
                        "@type", "Offer",
                        "priceCurrency", firstNonBlank(targetData.get("currency"), "IRR"),
                        "price", price,
                        "availability", "https://schema.org/InStock"
                ));
            }
        } else {
            schema.put("@type", "WebPage");
            schema.put("name", title);
            schema.put("description", description);
            schema.put("url", canonicalUrl);
            schema.put("isPartOf", Map.of("@type", "WebSite", "name", brandName));
        }
        return toJson(schema);
    }

    @SuppressWarnings("unchecked")
    private String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String string) {
            return "\"" + string.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value instanceof Map<?, ?> map) {
            return map.entrySet().stream()
                    .map(entry -> toJson(String.valueOf(entry.getKey())) + ":" + toJson(entry.getValue()))
                    .collect(Collectors.joining(",", "{", "}"));
        }
        if (value instanceof List<?> list) {
            return list.stream().map(this::toJson).collect(Collectors.joining(",", "[", "]"));
        }
        return toJson(String.valueOf(value));
    }

    private String absoluteUrl(Map<String, Object> route, Map<String, Object> domainBinding, String host) {
        String canonical = Objects.toString(route.get("canonicalUrl"), "");
        if (!canonical.isBlank()) {
            return canonical;
        }
        String path = Objects.toString(route.get("path"), "/");
        String boundHost = firstNonBlank(domainBinding.get("hostname"), host);
        if (!boundHost.isBlank()) {
            return "https://" + normalizeHost(boundHost) + normalizePath(path);
        }
        return path.startsWith("http") ? path : "https://example.com" + path;
    }

    private Map<String, Object> resolveDomainBinding(String host, String path, DynamicScope scope) {
        String normalizedHost = normalizeHost(host);
        if (normalizedHost.isBlank()) {
            return Map.of();
        }
        return dynamicRuntimeService.listRecords("domain-binding", scope).stream()
                .map(this::recordData)
                .filter(binding -> !"FAILED".equalsIgnoreCase(Objects.toString(binding.get("status"), "")))
                .filter(binding -> !"PENDING".equalsIgnoreCase(Objects.toString(binding.get("status"), "")))
                .filter(binding -> normalizedHost.equals(normalizeHost(Objects.toString(binding.get("hostname"), ""))))
                .filter(binding -> {
                    String targetPath = Objects.toString(binding.get("targetPath"), "");
                    return targetPath.isBlank() || normalizePath(targetPath).equals(normalizePath(path));
                })
                .findFirst()
                .or(() -> dynamicRuntimeService.listRecords("domain-binding", scope).stream()
                        .map(this::recordData)
                        .filter(binding -> normalizedHost.equals(normalizeHost(Objects.toString(binding.get("hostname"), ""))))
                        .findFirst())
                .orElse(Map.of());
    }

    private String normalizeHost(String host) {
        if (host == null || host.isBlank()) {
            return "";
        }
        String normalized = host.trim().toLowerCase();
        int commaIndex = normalized.indexOf(',');
        if (commaIndex >= 0) {
            normalized = normalized.substring(0, commaIndex).trim();
        }
        int colonIndex = normalized.indexOf(':');
        if (colonIndex >= 0) {
            normalized = normalized.substring(0, colonIndex);
        }
        return normalized;
    }

    private String defaulted(Object value, String fallback) {
        return value == null || String.valueOf(value).isBlank() ? fallback : String.valueOf(value);
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

    private String escapeXml(String input) {
        return escapeHtml(input).replace("'", "&apos;");
    }
}
