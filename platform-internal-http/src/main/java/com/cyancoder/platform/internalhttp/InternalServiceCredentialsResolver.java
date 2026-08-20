package com.cyancoder.platform.internalhttp;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public final class InternalServiceCredentialsResolver {
    private final Environment environment;

    public InternalServiceCredentialsResolver(Environment environment) {
        this.environment = environment;
    }

    public InternalServiceCredentials resolve(String targetServiceName) {
        String target = requireTargetServiceName(targetServiceName);
        String environmentPrefix = target.replaceAll("[^A-Za-z0-9]+", "_")
                .toUpperCase(Locale.ROOT);
        String fallbackPrefix = fallbackPrefix(target);

        String username = firstConfigured(propertyNames(target, environmentPrefix, "username"));
        String password = firstConfigured(propertyNames(target, environmentPrefix, "password"));
        return new InternalServiceCredentials(
                username == null ? fallbackPrefix + "_internal" : username,
                password == null ? fallbackPrefix + "_secret" : password
        );
    }

    public void applyBasicAuth(HttpHeaders headers, String targetServiceName) {
        InternalServiceCredentials credentials = resolve(targetServiceName);
        headers.setBasicAuth(credentials.username(), credentials.password(), StandardCharsets.UTF_8);
    }

    public String authorizationHeader(String targetServiceName) {
        InternalServiceCredentials credentials = resolve(targetServiceName);
        String value = credentials.username() + ":" + credentials.password();
        return "Basic " + Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private Set<String> propertyNames(String target, String environmentPrefix, String field) {
        Set<String> names = new LinkedHashSet<>();
        names.add(environmentPrefix + "_INTERNAL_" + field.toUpperCase(Locale.ROOT));
        names.add(target + ".internal." + field);
        names.add(target.replace('-', '.') + ".internal." + field);
        String base = target.endsWith("-service")
                ? target.substring(0, target.length() - "-service".length())
                : target;
        names.add(base + ".internal." + field);
        names.add(base.replace('-', '.') + ".internal." + field);
        names.add(base + ".service.internal." + field);
        names.add(base.replace('-', '.') + ".service.internal." + field);
        return names;
    }

    private String firstConfigured(Set<String> propertyNames) {
        for (String propertyName : propertyNames) {
            String value = environment.getProperty(propertyName);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String fallbackPrefix(String target) {
        String normalized = target.replaceAll("[^A-Za-z0-9]+", "_")
                .toLowerCase(Locale.ROOT);
        return normalized.endsWith("_service")
                ? normalized.substring(0, normalized.length() - "_service".length())
                : normalized;
    }

    private String requireTargetServiceName(String targetServiceName) {
        if (targetServiceName == null || targetServiceName.isBlank()) {
            throw new IllegalArgumentException("target service name is required");
        }
        return targetServiceName.trim();
    }
}
