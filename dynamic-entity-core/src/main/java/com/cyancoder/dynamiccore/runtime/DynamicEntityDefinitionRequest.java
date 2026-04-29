package com.cyancoder.dynamiccore.runtime;

public class DynamicEntityDefinitionRequest {
    private String entityKey;
    private String definitionJson;

    public String getEntityKey() { return entityKey; }
    public void setEntityKey(String entityKey) { this.entityKey = entityKey; }
    public String getDefinitionJson() { return definitionJson; }
    public void setDefinitionJson(String definitionJson) { this.definitionJson = definitionJson; }
}
