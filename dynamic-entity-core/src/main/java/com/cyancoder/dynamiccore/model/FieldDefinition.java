package com.cyancoder.dynamiccore.model;

import java.util.List;
import java.util.Map;

public class FieldDefinition {
    private String id;
    private String type;
    private List<ValidationRule> validations;
    private List<OperationRule> operations;
    private Map<String, FieldDefinition> itemValidations;
    private Object defaultValue;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public List<ValidationRule> getValidations() { return validations; }
    public void setValidations(List<ValidationRule> validations) { this.validations = validations; }
    public List<OperationRule> getOperations() { return operations; }
    public void setOperations(List<OperationRule> operations) { this.operations = operations; }
    public Map<String, FieldDefinition> getItemValidations() { return itemValidations; }
    public void setItemValidations(Map<String, FieldDefinition> itemValidations) { this.itemValidations = itemValidations; }
    public Object getDefaultValue() { return defaultValue; }
    public void setDefaultValue(Object defaultValue) { this.defaultValue = defaultValue; }
}
