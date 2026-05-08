package com.cyancoder.aiorchestrator.domain;

import java.util.LinkedHashMap;
import java.util.Map;

public class FlowBlueprint {
    private String flowKey;
    private Map<String, Object> flowDefinition = new LinkedHashMap<>();

    public String getFlowKey() { return flowKey; }
    public void setFlowKey(String flowKey) { this.flowKey = flowKey; }
    public Map<String, Object> getFlowDefinition() { return flowDefinition; }
    public void setFlowDefinition(Map<String, Object> flowDefinition) { this.flowDefinition = flowDefinition; }
}
