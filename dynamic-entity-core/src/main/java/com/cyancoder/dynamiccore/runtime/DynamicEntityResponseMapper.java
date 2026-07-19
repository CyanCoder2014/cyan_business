package com.cyancoder.dynamiccore.runtime;

import com.cyancoder.dynamiccore.model.EntityDefinitionModel;
import com.cyancoder.dynamiccore.service.DynamicDefinitionParser;
import com.cyancoder.dynamiccore.store.jpa.StoredEntityDefinition;
import com.cyancoder.dynamiccore.template.DynamicEntityTemplate;

public class DynamicEntityResponseMapper {

    private final DynamicDefinitionParser definitionParser;

    public DynamicEntityResponseMapper(DynamicDefinitionParser definitionParser) {
        this.definitionParser = definitionParser;
    }

    public DynamicEntityDefinitionResponse toDefinitionResponse(StoredEntityDefinition stored) {
        return new DynamicEntityDefinitionResponse(
                stored.getId(),
                stored.getServiceKey(),
                stored.getTenantKey(),
                stored.getSiteKey(),
                stored.getEntityKey(),
                stored.getEntityType(),
                stored.getTitle(),
                parseDefinition(stored.getDefinitionJson()),
                stored.isActive(),
                stored.getCreatedAt(),
                stored.getUpdatedAt()
        );
    }

    public DynamicEntityTemplateResponse toTemplateResponse(DynamicEntityTemplate template) {
        return new DynamicEntityTemplateResponse(
                template.getTemplateKey(),
                template.getEntityType(),
                template.getTitle(),
                template.getDescription(),
                parseDefinition(template.getDefinitionJson())
        );
    }

    private EntityDefinitionModel parseDefinition(String definitionJson) {
        return definitionJson == null ? null : definitionParser.parse(definitionJson);
    }
}
