package com.cyancoder.batchworker.service;

import com.cyancoder.batchworker.api.BatchDefinitionSpec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemStreamReader;
import org.springframework.web.util.UriComponentsBuilder;

public class ApiBatchReader implements ItemStreamReader<Map<String, Object>> {
    private final BatchDefinitionSpec.Source source;
    private final ObjectMapper objectMapper;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private int page;
    private int index;
    private List<Map<String, Object>> current = List.of();
    private boolean exhausted;

    public ApiBatchReader(BatchDefinitionSpec.Source source, ObjectMapper objectMapper) {
        this.source = source;
        this.objectMapper = objectMapper;
    }

    @Override
    public void open(ExecutionContext context) {
        page = context.getInt("source.page", 0);
        index = context.getInt("source.index", 0);
    }

    @Override
    public Map<String, Object> read() {
        if (exhausted) {
            return null;
        }
        if (current.isEmpty()) {
            current = load(page);
            if (current.isEmpty() || index >= current.size()) {
                exhausted = true;
                return null;
            }
        }
        Map<String, Object> item = current.get(index++);
        if (index >= current.size()) {
            int configuredSize = pageSize();
            if (current.size() < configuredSize) {
                exhausted = true;
            } else {
                page++;
                index = 0;
                current = List.of();
            }
        }
        return item;
    }

    @Override
    public void update(ExecutionContext context) {
        context.putInt("source.page", page);
        context.putInt("source.index", index);
    }

    private List<Map<String, Object>> load(int pageNumber) {
        URI uri = UriComponentsBuilder.fromUriString(source.url())
                .queryParam(defaultValue(source.pageParameter(), "page"), pageNumber)
                .queryParam(defaultValue(source.sizeParameter(), "size"), pageSize())
                .build(true).toUri();
        HttpRequest.Builder request = HttpRequest.newBuilder(uri).GET().timeout(Duration.ofSeconds(30));
        applyHeaders(request, source.headers(), source.bearerTokenEnvironmentVariable());
        try {
            HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 429 || response.statusCode() >= 500) {
                throw new RetryableApiException("Source API returned HTTP " + response.statusCode());
            }
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Source API returned HTTP " + response.statusCode());
            }
            JsonNode node = objectMapper.readTree(response.body());
            for (String part : defaultValue(source.itemsPath(), "").split("\\.")) {
                if (!part.isBlank()) {
                    node = node.path(part);
                }
            }
            if (!node.isArray()) {
                throw new IllegalStateException("Source itemsPath does not resolve to an array");
            }
            List<Map<String, Object>> result = new ArrayList<>();
            for (JsonNode item : node) {
                result.add(objectMapper.convertValue(item, LinkedHashMap.class));
            }
            return result;
        } catch (RetryableApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new RetryableApiException("Source API request failed", exception);
        }
    }

    static void applyHeaders(HttpRequest.Builder builder, Map<String, String> headers, String tokenEnv) {
        if (headers != null) {
            headers.forEach(builder::header);
        }
        if (tokenEnv != null && !tokenEnv.isBlank()) {
            String token = System.getenv(tokenEnv);
            if (token == null || token.isBlank()) {
                throw new IllegalStateException("Missing credential environment variable: " + tokenEnv);
            }
            builder.header("Authorization", token.startsWith("Bearer ") ? token : "Bearer " + token);
        }
    }

    private int pageSize() {
        return source.pageSize() == null || source.pageSize() < 1 ? 200 : source.pageSize();
    }

    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
