package com.cyancoder.batchworker.api;

import com.cyancoder.batchworker.domain.BatchDefinition;
import java.time.Instant;
import java.util.UUID;

public record BatchDefinitionResponse(
        UUID id, String tenantKey, String siteKey, String definitionKey, String title,
        boolean active, BatchDefinitionSpec spec, Instant createdAt, Instant updatedAt
) {
    public static BatchDefinitionResponse from(BatchDefinition value, BatchDefinitionSpec spec) {
        return new BatchDefinitionResponse(value.getId(), value.getTenantKey(), value.getSiteKey(),
                value.getDefinitionKey(), value.getTitle(), value.isActive(), spec,
                value.getCreatedAt(), value.getUpdatedAt());
    }
}
