package com.cyancoder.dynamiccore.operator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OperatorRegistry {
    private final Map<String, DynamicOperator> operators = new HashMap<>();

    public OperatorRegistry(List<DynamicOperator> operatorList) {
        for (DynamicOperator operator : operatorList) {
            operators.put(operator.name().toUpperCase(), operator);
        }
    }

    public DynamicOperator get(String name) {
        DynamicOperator operator = operators.get(name == null ? "" : name.toUpperCase());
        if (operator == null) {
            throw new IllegalArgumentException("operator not found: " + name);
        }
        return operator;
    }
}
