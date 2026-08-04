package com.cyancoder.batchworker.api;

import java.util.Map;

public record BatchDefinitionSpec(
        Source source,
        Destination destination,
        Map<String, String> fieldMappings,
        Integer chunkSize,
        Integer retryLimit,
        Integer skipLimit
) {
    public record Source(
            String url,
            String itemsPath,
            String pageParameter,
            String sizeParameter,
            Integer pageSize,
            Map<String, String> headers,
            String bearerTokenEnvironmentVariable,
            Authentication authentication
    ) {}

    public record Destination(
            String url,
            String method,
            String itemKeyPath,
            Map<String, String> headers,
            String bearerTokenEnvironmentVariable,
            Authentication authentication
    ) {}

    /**
     * Secret-safe outbound authentication. The secret is resolved only in the
     * worker process and is never persisted in a definition or job parameter.
     *
     * <p>Supported types are {@code BASIC} and {@code BEARER}. BASIC requires a
     * username (literal or environment variable) and a secret environment
     * variable. BEARER requires only the secret environment variable.</p>
     */
    public record Authentication(
            String type,
            String username,
            String usernameEnvironmentVariable,
            String secretEnvironmentVariable
    ) {}
}
