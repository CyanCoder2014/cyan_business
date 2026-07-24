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
            String bearerTokenEnvironmentVariable
    ) {}

    public record Destination(
            String url,
            String method,
            String itemKeyPath,
            Map<String, String> headers,
            String bearerTokenEnvironmentVariable
    ) {}
}
