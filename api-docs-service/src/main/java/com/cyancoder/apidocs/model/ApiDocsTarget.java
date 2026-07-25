package com.cyancoder.apidocs.model;

public record ApiDocsTarget(
        String serviceKey,
        String baseUrl,
        String docsPath,
        String username,
        String password,
        String passwordEnvironmentVariable,
        Boolean enabled
) {
    public String resolvedDocsPath() {
        return docsPath == null || docsPath.isBlank() ? "/v3/api-docs" : docsPath;
    }

    public boolean isEnabled() {
        return enabled == null || enabled;
    }
}
