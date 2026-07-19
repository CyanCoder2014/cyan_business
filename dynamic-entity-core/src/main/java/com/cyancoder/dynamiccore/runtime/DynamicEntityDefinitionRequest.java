package com.cyancoder.dynamiccore.runtime;

import com.cyancoder.dynamiccore.model.EntityDefinitionModel;

public class DynamicEntityDefinitionRequest {
    private String entityKey;
    private String tenantKey;
    private String siteKey;
    private EntityDefinitionModel definition;
    private String definitionJson;

    public String getEntityKey() { return entityKey; }
    public void setEntityKey(String entityKey) { this.entityKey = entityKey; }
    public String getTenantKey() { return tenantKey; }
    public void setTenantKey(String tenantKey) { this.tenantKey = tenantKey; }
    public String getSiteKey() { return siteKey; }
    public void setSiteKey(String siteKey) { this.siteKey = siteKey; }
    public EntityDefinitionModel getDefinition() { return definition; }
    public void setDefinition(EntityDefinitionModel definition) { this.definition = definition; }
    public String getDefinitionJson() { return definitionJson; }
    public void setDefinitionJson(String definitionJson) { this.definitionJson = definitionJson; }
}
