package com.cyancoder.batchworker.service;

import com.cyancoder.batchworker.api.BatchDefinitionSpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

public class ApiBatchWriter implements ItemWriter<Map<String, Object>> {
    private final BatchDefinitionSpec.Destination destination;
    private final ObjectMapper objectMapper;
    private final String idempotencyPrefix;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public ApiBatchWriter(BatchDefinitionSpec.Destination destination, ObjectMapper objectMapper,
            String idempotencyPrefix) {
        this.destination = destination;
        this.objectMapper = objectMapper;
        this.idempotencyPrefix = idempotencyPrefix;
    }

    @Override
    public void write(Chunk<? extends Map<String, Object>> chunk) throws Exception {
        for (Map<String, Object> item : chunk) {
            byte[] body = objectMapper.writeValueAsBytes(item);
            String itemKey = valueAt(item, destination.itemKeyPath());
            if (itemKey == null || itemKey.isBlank()) {
                itemKey = sha256(body);
            }
            HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofByteArray(body);
            String method = destination.method() == null ? "POST" : destination.method().toUpperCase();
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(destination.url()))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Idempotency-Key", "batch:" + sha256(
                            (idempotencyPrefix + ":" + itemKey).getBytes(StandardCharsets.UTF_8)))
                    .method(method, publisher);
            ApiBatchReader.applyHeaders(request, destination.headers(),
                    destination.bearerTokenEnvironmentVariable());
            HttpResponse<String> response;
            try {
                response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
            } catch (Exception exception) {
                throw new RetryableApiException("Destination API request failed", exception);
            }
            if (response.statusCode() == 429 || response.statusCode() >= 500) {
                throw new RetryableApiException("Destination API returned HTTP " + response.statusCode());
            }
            if (response.statusCode() >= 400) {
                throw new SkippableItemException("Destination rejected item with HTTP "
                        + response.statusCode() + ": " + truncate(response.body()));
            }
        }
    }

    @SuppressWarnings("unchecked")
    static String valueAt(Map<String, Object> item, String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        Object value = item;
        for (String part : path.split("\\.")) {
            if (!(value instanceof Map<?, ?> map)) {
                return null;
            }
            value = map.get(part);
        }
        return value == null ? null : value.toString();
    }

    static Map<String, Object> mapFields(Map<String, Object> item, Map<String, String> mappings) {
        if (mappings == null || mappings.isEmpty()) {
            return item;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        mappings.forEach((target, source) -> putAt(result, target, valueObjectAt(item, source)));
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Object valueObjectAt(Map<String, Object> item, String path) {
        Object value = item;
        for (String part : path.split("\\.")) {
            if (!(value instanceof Map<?, ?> map)) {
                return null;
            }
            value = map.get(part);
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private static void putAt(Map<String, Object> result, String path, Object value) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = result;
        for (int i = 0; i < parts.length - 1; i++) {
            current = (Map<String, Object>) current.computeIfAbsent(parts[i], ignored -> new LinkedHashMap<>());
        }
        current.put(parts[parts.length - 1], value);
    }

    private String sha256(byte[] value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
    }

    private String truncate(String value) {
        if (value == null) return "";
        return value.substring(0, Math.min(value.length(), 500));
    }
}
