package com.cyancoder.batchworker.service;

import com.cyancoder.batchworker.api.BatchDefinitionSpec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
        applyHeaders(request, source.headers(), source.bearerTokenEnvironmentVariable(),
                source.authentication());
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

    static void applyHeaders(
            HttpRequest.Builder builder,
            Map<String, String> headers,
            String tokenEnv,
            BatchDefinitionSpec.Authentication authentication
    ) {
        applyHeaders(builder, headers, tokenEnv, authentication, System::getenv);
    }

    static void applyHeaders(
            HttpRequest.Builder builder,
            Map<String, String> headers,
            String tokenEnv,
            BatchDefinitionSpec.Authentication authentication,
            Function<String, String> environment
    ) {
        if (headers != null) {
            headers.forEach(builder::header);
        }
        if (tokenEnv != null && !tokenEnv.isBlank() && authentication != null) {
            throw new IllegalStateException(
                    "Configure bearerTokenEnvironmentVariable or authentication, not both");
        }
        if (tokenEnv != null && !tokenEnv.isBlank()) {
            String token = requiredEnvironment(environment, tokenEnv);
            builder.header("Authorization", token.startsWith("Bearer ") ? token : "Bearer " + token);
            return;
        }
        if (authentication == null || authentication.type() == null
                || authentication.type().isBlank()) {
            return;
        }
        String type = authentication.type().trim().toUpperCase();
        String secret = requiredEnvironment(environment, authentication.secretEnvironmentVariable());
        if ("BEARER".equals(type)) {
            builder.header("Authorization", secret.startsWith("Bearer ") ? secret : "Bearer " + secret);
            return;
        }
        if ("BASIC".equals(type)) {
            String username = authentication.username();
            if ((username == null || username.isBlank())
                    && authentication.usernameEnvironmentVariable() != null
                    && !authentication.usernameEnvironmentVariable().isBlank()) {
                username = requiredEnvironment(
                        environment, authentication.usernameEnvironmentVariable());
            }
            if (username == null || username.isBlank()) {
                throw new IllegalStateException("BASIC authentication requires a username");
            }
            String value = Base64.getEncoder().encodeToString(
                    (username + ":" + secret).getBytes(StandardCharsets.UTF_8));
            builder.header("Authorization", "Basic " + value);
            return;
        }
        throw new IllegalStateException("Unsupported batch authentication type: " + type);
    }

    private static String requiredEnvironment(
            Function<String, String> environment, String variableName) {
        if (variableName == null || variableName.isBlank()) {
            throw new IllegalStateException("Credential environment variable is required");
        }
        String value = environment.apply(variableName);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing credential environment variable: " + variableName);
        }
        return value;
    }

    private int pageSize() {
        return source.pageSize() == null || source.pageSize() < 1 ? 200 : source.pageSize();
    }

    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
