package com.cyancoder.dynamiccore.model;

import java.util.List;
import java.util.Map;

public class EntityDefinitionModel {
    private String serviceKey;
    private String entityKey;
    private String entityType;
    private String title;
    private Map<String, FieldDefinition> fields;
    private List<ValidationRule> validations;
    private List<OperationRule> operations;
    private Map<String, Object> defaultValues;
    private Map<String, Object> relationDefinitions;

    public String getServiceKey() { return serviceKey; }
    public void setServiceKey(String serviceKey) { this.serviceKey = serviceKey; }
    public String getEntityKey() { return entityKey; }
    public void setEntityKey(String entityKey) { this.entityKey = entityKey; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Map<String, FieldDefinition> getFields() { return fields; }
    public void setFields(Map<String, FieldDefinition> fields) { this.fields = fields; }
    public List<ValidationRule> getValidations() { return validations; }
    public void setValidations(List<ValidationRule> validations) { this.validations = validations; }
    public List<OperationRule> getOperations() { return operations; }
    public void setOperations(List<OperationRule> operations) { this.operations = operations; }
    public Map<String, Object> getDefaultValues() { return defaultValues; }
    public void setDefaultValues(Map<String, Object> defaultValues) { this.defaultValues = defaultValues; }
    public Map<String, Object> getRelationDefinitions() { return relationDefinitions; }
    public void setRelationDefinitions(Map<String, Object> relationDefinitions) { this.relationDefinitions = relationDefinitions; }
}
