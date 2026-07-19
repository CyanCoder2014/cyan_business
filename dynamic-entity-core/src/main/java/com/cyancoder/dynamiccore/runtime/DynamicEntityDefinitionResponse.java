package com.cyancoder.dynamiccore.runtime;

import com.cyancoder.dynamiccore.model.EntityDefinitionModel;

import java.time.Instant;

public record DynamicEntityDefinitionResponse(
        Long id,
        String serviceKey,
        String tenantKey,
        String siteKey,
        String entityKey,
        String entityType,
        String title,
        EntityDefinitionModel definition,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
