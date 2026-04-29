package com.cyancoder.dynamiccore.model;

import java.util.Map;

public class OperationRule {
    private String id;
    private int order;
    private String type;
    private String operation;
    private Map<String, Object> operationParams;
    private String operationMessage;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    public Map<String, Object> getOperationParams() { return operationParams; }
    public void setOperationParams(Map<String, Object> operationParams) { this.operationParams = operationParams; }
    public String getOperationMessage() { return operationMessage; }
    public void setOperationMessage(String operationMessage) { this.operationMessage = operationMessage; }
}
