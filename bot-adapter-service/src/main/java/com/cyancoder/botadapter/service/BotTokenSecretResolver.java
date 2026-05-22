package com.cyancoder.botadapter.service;

import com.cyancoder.botadapter.config.AiOrchestratorProperties;
import com.cyancoder.botadapter.domain.BotChannelIntegration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

@Component
public class BotTokenSecretResolver {
    private final AiOrchestratorProperties properties;

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
        if (secretRef.startsWith("env://")) {
            return System.getenv(secretRef.substring("env://".length()));
        }
        if (secretRef.startsWith("file://")) {
            return readSecretFile(secretRef.substring("file://".length()));
        }

        String inline = properties.getBotSecretValues().get(secretRef);
        if (inline != null && !inline.isBlank()) {
            return inline;
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
}
