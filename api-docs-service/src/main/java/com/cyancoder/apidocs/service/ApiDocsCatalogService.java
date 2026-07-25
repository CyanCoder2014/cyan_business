package com.cyancoder.apidocs.service;

import com.cyancoder.apidocs.config.ApiDocsCatalogProperties;
import com.cyancoder.apidocs.model.ApiDocsServiceSummary;
import com.cyancoder.apidocs.model.ApiDocsTarget;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ApiDocsCatalogService {
    private final ApiDocsTargetRegistry registry;
    private final ApiDocsCatalogProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient client;
    private final Map<String, CachedDocument> cache = new ConcurrentHashMap<>();

    public ApiDocsCatalogService(
            ApiDocsTargetRegistry registry,
            ApiDocsCatalogProperties properties,
            ObjectMapper objectMapper
    ) {
        this.registry = registry;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(1, properties.getConnectTimeoutMs())))
                .build();
    }

    public List<ApiDocsServiceSummary> list() {
        return registry.list().parallelStream()
                .map(this::loadSummary)
                .sorted(Comparator.comparing(ApiDocsServiceSummary::serviceKey))
                .toList();
    }

    public JsonNode get(String serviceKey, boolean refresh) {
        return load(registry.get(serviceKey), refresh).document().deepCopy();
    }

    public ObjectNode aggregate(boolean refresh) {
        ObjectNode aggregate = objectMapper.createObjectNode();
        aggregate.put("openapi", "3.1.0");
        ObjectNode info = aggregate.putObject("info");
        info.put("title", "Cyan Business Platform");
        info.put("version", "1.0.0");
        ObjectNode paths = aggregate.putObject("paths");
        ObjectNode components = aggregate.putObject("components");
        components.putObject("schemas");
        ObjectNode securitySchemes = components.putObject("securitySchemes");
        securitySchemes.putObject("bearerAuth")
                .put("type", "http")
                .put("scheme", "bearer")
                .put("bearerFormat", "JWT");
        securitySchemes.putObject("basicAuth")
                .put("type", "http")
                .put("scheme", "basic");
        ArrayNode services = aggregate.putArray("x-platform-services");
        ArrayNode unavailable = aggregate.putArray("x-platform-unavailable-services");

        List<TargetLoadResult> loadedTargets = registry.list().parallelStream()
                .map(target -> loadResult(target, refresh))
                .toList();
        for (TargetLoadResult loaded : loadedTargets) {
            ApiDocsTarget target = loaded.target();
            if (loaded.document() != null) {
                CachedDocument cached = loaded.document();
                JsonNode document = cached.document();
                ObjectNode service = services.addObject();
                service.put("serviceKey", target.serviceKey());
                service.put("baseUrl", target.baseUrl());
                copyPaths(target, document.path("paths"), paths);
                copyComponents(target, document.path("components"), components);
            } else {
                ObjectNode issue = unavailable.addObject();
                issue.put("serviceKey", target.serviceKey());
                issue.put("error", loaded.error());
            }
        }
        return aggregate;
    }

    private TargetLoadResult loadResult(ApiDocsTarget target, boolean refresh) {
        try {
            return new TargetLoadResult(target, load(target, refresh), null);
        } catch (RuntimeException exception) {
            return new TargetLoadResult(target, null, safeMessage(exception));
        }
    }

    private ApiDocsServiceSummary loadSummary(ApiDocsTarget target) {
        try {
            CachedDocument document = load(target, false);
            return summary(target, document, null);
        } catch (RuntimeException exception) {
            return new ApiDocsServiceSummary(
                    target.serviceKey(),
                    target.baseUrl(),
                    "UNAVAILABLE",
                    null,
                    null,
                    0,
                    Instant.now(),
                    safeMessage(exception));
        }
    }

    private void copyPaths(ApiDocsTarget target, JsonNode source, ObjectNode destination) {
        if (!source.isObject()) {
            return;
        }
        source.properties().forEach(entry -> {
            String mergedPath = "/services/" + target.serviceKey() + entry.getKey();
            JsonNode pathItem = rewriteReferences(entry.getValue(), target.serviceKey());
            if (pathItem instanceof ObjectNode object) {
                object.properties().forEach(operationEntry -> {
                    if (operationEntry.getValue() instanceof ObjectNode operation
                            && isHttpMethod(operationEntry.getKey())) {
                        operation.put("x-platform-service-key", target.serviceKey());
                        operation.put("x-platform-original-path", entry.getKey());
                    }
                });
            }
            destination.set(mergedPath, pathItem);
        });
    }

    private void copyComponents(
            ApiDocsTarget target,
            JsonNode source,
            ObjectNode destination
    ) {
        if (!source.isObject()) {
            return;
        }
        source.properties().forEach(section -> {
            if ("securitySchemes".equals(section.getKey())
                    || !section.getValue().isObject()) {
                return;
            }
            JsonNode existing = destination.get(section.getKey());
            ObjectNode destinationSection = existing instanceof ObjectNode object
                    ? object
                    : destination.putObject(section.getKey());
            section.getValue().properties().forEach(entry -> destinationSection.set(
                    schemaPrefix(target.serviceKey()) + entry.getKey(),
                    rewriteReferences(entry.getValue(), target.serviceKey())));
        });
    }

    private JsonNode rewriteReferences(JsonNode source, String serviceKey) {
        JsonNode copy = source.deepCopy();
        rewrite(copy, serviceKey);
        return copy;
    }

    private void rewrite(JsonNode node, String serviceKey) {
        if (node instanceof ObjectNode object) {
            object.properties().forEach(entry -> {
                if ("$ref".equals(entry.getKey()) && entry.getValue().isTextual()) {
                    object.put(
                            entry.getKey(),
                            rewriteComponentReference(
                                    entry.getValue().asText(), serviceKey));
                } else {
                    rewrite(entry.getValue(), serviceKey);
                }
            });
        } else if (node instanceof ArrayNode array) {
            array.forEach(item -> rewrite(item, serviceKey));
        }
    }

    private String rewriteComponentReference(String reference, String serviceKey) {
        String prefix = "#/components/";
        if (!reference.startsWith(prefix)) {
            return reference;
        }
        int componentNameStart = reference.indexOf('/', prefix.length());
        if (componentNameStart < 0 || componentNameStart == reference.length() - 1) {
            return reference;
        }
        return reference.substring(0, componentNameStart + 1)
                + schemaPrefix(serviceKey)
                + reference.substring(componentNameStart + 1);
    }

    private CachedDocument load(ApiDocsTarget target, boolean refresh) {
        CachedDocument existing = cache.get(target.serviceKey());
        Instant now = Instant.now();
        if (!refresh && existing != null
                && existing.fetchedAt().plusSeconds(Math.max(0, properties.getCacheSeconds()))
                .isAfter(now)) {
            return existing;
        }
        HttpRequest.Builder request = HttpRequest.newBuilder(targetUri(target))
                .timeout(Duration.ofMillis(Math.max(1, properties.getReadTimeoutMs())))
                .header("Accept", "application/json")
                .GET();
        if (target.username() != null && !target.username().isBlank()) {
            String password = target.password();
            if ((password == null || password.isBlank())
                    && target.passwordEnvironmentVariable() != null
                    && !target.passwordEnvironmentVariable().isBlank()) {
                password = System.getenv(target.passwordEnvironmentVariable());
            }
            if (password == null) {
                throw new IllegalArgumentException(
                        "API docs target password is required: " + target.serviceKey());
            }
            String encoded = Base64.getEncoder().encodeToString(
                    (target.username() + ":" + password)
                            .getBytes(StandardCharsets.UTF_8));
            request.header("Authorization", "Basic " + encoded);
        }
        try {
            HttpResponse<String> response = client.send(
                    request.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        target.serviceKey() + " OpenAPI returned HTTP " + response.statusCode());
            }
            JsonNode document = objectMapper.readTree(response.body());
            if (!document.path("paths").isObject()) {
                throw new IllegalStateException(
                        target.serviceKey() + " OpenAPI has no paths object");
            }
            CachedDocument loaded = new CachedDocument(document, now);
            cache.put(target.serviceKey(), loaded);
            return loaded;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Interrupted while loading OpenAPI for " + target.serviceKey(), exception);
        } catch (Exception exception) {
            throw exception instanceof RuntimeException runtime
                    ? runtime
                    : new IllegalStateException(
                    "Failed to load OpenAPI for " + target.serviceKey(), exception);
        }
    }

    private ApiDocsServiceSummary summary(
            ApiDocsTarget target,
            CachedDocument cached,
            String error
    ) {
        JsonNode document = cached.document();
        return new ApiDocsServiceSummary(
                target.serviceKey(),
                target.baseUrl(),
                error == null ? "AVAILABLE" : "UNAVAILABLE",
                document.path("info").path("title").asText(null),
                document.path("info").path("version").asText(null),
                document.path("paths").size(),
                cached.fetchedAt(),
                error);
    }

    private URI targetUri(ApiDocsTarget target) {
        String base = target.baseUrl().endsWith("/")
                ? target.baseUrl().substring(0, target.baseUrl().length() - 1)
                : target.baseUrl();
        String path = target.resolvedDocsPath().startsWith("/")
                ? target.resolvedDocsPath()
                : "/" + target.resolvedDocsPath();
        return URI.create(base + path);
    }

    private boolean isHttpMethod(String value) {
        return switch (value.toLowerCase()) {
            case "get", "put", "post", "delete", "patch", "head", "options", "trace" -> true;
            default -> false;
        };
    }

    private String schemaPrefix(String serviceKey) {
        StringBuilder result = new StringBuilder();
        for (String part : serviceKey.split("[^A-Za-z0-9]+")) {
            if (!part.isBlank()) {
                result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return result + "_";
    }

    private String safeMessage(RuntimeException exception) {
        String message = exception.getMessage();
        return message == null ? exception.getClass().getSimpleName() : message;
    }

    private record CachedDocument(JsonNode document, Instant fetchedAt) {
    }

    private record TargetLoadResult(
            ApiDocsTarget target,
            CachedDocument document,
            String error
    ) {
    }
}
