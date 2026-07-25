package com.cyancoder.apidocs.model;

import java.time.Instant;

public record ApiDocsServiceSummary(
        String serviceKey,
        String baseUrl,
        String status,
        String title,
        String version,
        int pathCount,
        Instant fetchedAt,
        String error
) {
}
