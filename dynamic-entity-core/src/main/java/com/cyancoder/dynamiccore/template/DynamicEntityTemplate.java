package com.cyancoder.dynamiccore.template;

public class DynamicEntityTemplate {

    private final String templateKey;
    private final String entityType;
    private final String title;
    private final String description;
    private final String definitionJson;

    public DynamicEntityTemplate(String templateKey, String entityType, String title, String description, String definitionJson) {
        this.templateKey = templateKey;
        this.entityType = entityType;
        this.title = title;
        this.description = description;
        this.definitionJson = definitionJson;
    }

    public String getTemplateKey() {
        return templateKey;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDefinitionJson() {
        return definitionJson;
    }
}
