package com.cyancoder.dynamiccore.model;

import java.util.Map;

public class ValidationRule {
    private String id;
    private int order;
    private String type;
    private String validation;
    private Map<String, Object> validationParams;
    private String validationMessage;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getValidation() { return validation; }
    public void setValidation(String validation) { this.validation = validation; }
    public Map<String, Object> getValidationParams() { return validationParams; }
    public void setValidationParams(Map<String, Object> validationParams) { this.validationParams = validationParams; }
    public String getValidationMessage() { return validationMessage; }
    public void setValidationMessage(String validationMessage) { this.validationMessage = validationMessage; }
}
