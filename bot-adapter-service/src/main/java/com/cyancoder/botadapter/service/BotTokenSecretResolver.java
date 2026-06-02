package com.cyancoder.botadapter.service;

import com.cyancoder.botadapter.config.AiOrchestratorProperties;
import com.cyancoder.botadapter.domain.BotChannelIntegration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

@Component
public class BotTokenSecretResolver {
    private final AiOrchestratorProperties properties;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public BotTokenSecretResolver(AiOrchestratorProperties properties) {
        this.properties = properties;
    }

    public String resolveToken(BotChannelIntegration integration) {
        String secretRef = integration.getTokenSecretRef();
        if (secretRef != null && !secretRef.isBlank()) {
            String resolved = resolveByReference(secretRef.trim());
            if (resolved != null && !resolved.isBlank()) {
                return resolved.trim();
            }
            throw new IllegalStateException("Unable to resolve bot token from secret reference " + secretRef);
        }
        if (integration.getManagedBotToken() != null && !integration.getManagedBotToken().isBlank()) {
            return integration.getManagedBotToken().trim();
        }
        throw new IllegalStateException("No bot token or secret reference is configured for integration " + integration.getIntegrationKey());
    }

    private String resolveByReference(String secretRef) {
        String inline = properties.getBotSecretValues().get(secretRef);
        if (inline != null && !inline.isBlank()) {
            return inline;
        }

        if (secretRef.startsWith("env://")) {
            return System.getenv(secretRef.substring("env://".length()));
        }
        if (secretRef.startsWith("file://")) {
            return readSecretFile(secretRef.substring("file://".length()));
        }
        if (secretRef.startsWith("vault://")) {
            return fetchExternalSecret(secretRef.substring("vault://".length()));
        }

        String envValue = System.getenv(toEnvKey(secretRef));
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        Map<String, String> fileSecrets = loadSecretMap();
        String mapped = fileSecrets.get(secretRef);
        if (mapped != null && !mapped.isBlank()) {
            return mapped;
        }
        return null;
    }

    private String fetchExternalSecret(String secretPath) {
        if (properties.getBotSecretHttpBaseUrl() == null || properties.getBotSecretHttpBaseUrl().isBlank()) {
            return null;
        }
        String normalizedBaseUrl = properties.getBotSecretHttpBaseUrl().replaceAll("/+$", "");
        String normalizedPath = secretPath.replaceAll("^/+", "");
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(normalizedBaseUrl + "/" + normalizedPath))
                .GET()
                .header("Accept", "text/plain, application/json");
        if (properties.getBotSecretHttpAuthToken() != null && !properties.getBotSecretHttpAuthToken().isBlank()) {
            builder.header("Authorization", "Bearer " + properties.getBotSecretHttpAuthToken());
        }
        try {
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("External secret lookup failed with status " + response.statusCode());
            }
            String body = response.body() == null ? "" : response.body().trim();
            if (body.startsWith("{") && body.endsWith("}")) {
                String extracted = extractJsonValue(body, "value");
                if (extracted != null && !extracted.isBlank()) {
                    return extracted;
                }
                extracted = extractJsonValue(body, "secret");
                if (extracted != null && !extracted.isBlank()) {
                    return extracted;
                }
            }
            return body;
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Unable to resolve external bot token secret for " + secretPath, ex);
        }
    }

    private String readSecretFile(String pathValue) {
        try {
            return Files.readString(Path.of(pathValue)).trim();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read bot token secret file " + pathValue, ex);
        }
    }

    private Map<String, String> loadSecretMap() {
        if (properties.getBotSecretsFile() == null || properties.getBotSecretsFile().isBlank()) {
            return Map.of();
        }
        Properties loaded = new Properties();
        try (var stream = Files.newInputStream(Path.of(properties.getBotSecretsFile()))) {
            loaded.load(stream);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load bot secrets file " + properties.getBotSecretsFile(), ex);
        }
        Map<String, String> secrets = new LinkedHashMap<>();
        for (String name : loaded.stringPropertyNames()) {
            secrets.put(name, loaded.getProperty(name));
        }
        return secrets;
    }

    private String toEnvKey(String secretRef) {
        String normalized = secretRef
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
        return properties.getBotSecretEnvPrefix() + normalized;
    }

    private String extractJsonValue(String body, String key) {
        String marker = "\"" + key + "\"";
        int index = body.indexOf(marker);
        if (index < 0) {
            return null;
        }
        int colonIndex = body.indexOf(':', index + marker.length());
        int firstQuoteIndex = body.indexOf('"', colonIndex + 1);
        int secondQuoteIndex = body.indexOf('"', firstQuoteIndex + 1);
        if (colonIndex < 0 || firstQuoteIndex < 0 || secondQuoteIndex < 0) {
            return null;
        }
        return body.substring(firstQuoteIndex + 1, secondQuoteIndex);
    }
}
