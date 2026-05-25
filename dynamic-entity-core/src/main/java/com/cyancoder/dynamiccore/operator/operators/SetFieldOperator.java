package com.cyancoder.dynamiccore.operator.operators;

import com.cyancoder.dynamiccore.model.OperationRule;
import com.cyancoder.dynamiccore.operator.DynamicOperator;

import java.util.Map;

public class SetFieldOperator implements DynamicOperator {
    @Override
    public String name() {
        return "SET_FIELD";
    }

    @Override
    public Map<String, Object> apply(OperationRule rule, String path, Object value, Map<String, Object> currentObject, Map<String, Object> fullInput) {
        String field = String.valueOf(rule.getOperationParams().getOrDefault("field", path));
        fullInput.put(field, rule.getOperationParams().get("value"));
        return fullInput;
    }
}
