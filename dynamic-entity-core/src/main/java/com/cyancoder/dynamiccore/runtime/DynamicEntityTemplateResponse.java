package com.cyancoder.dynamiccore.runtime;

import com.cyancoder.dynamiccore.model.EntityDefinitionModel;

public record DynamicEntityTemplateResponse(
        String templateKey,
        String entityType,
        String title,
        String description,
        EntityDefinitionModel definition
) {
}
