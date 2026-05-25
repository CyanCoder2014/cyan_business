package com.cyancoder.dynamiccore.operator.operators;

import com.cyancoder.dynamiccore.model.OperationRule;
import com.cyancoder.dynamiccore.operator.DynamicOperator;

import java.util.Map;

public class CopyFieldOperator implements DynamicOperator {
    @Override
    public String name() {
        return "COPY_FIELD";
    }

    @Override
    public Map<String, Object> apply(OperationRule rule, String path, Object value, Map<String, Object> currentObject, Map<String, Object> fullInput) {
        String source = String.valueOf(rule.getOperationParams().get("sourceField"));
        String target = String.valueOf(rule.getOperationParams().get("targetField"));
        fullInput.put(target, fullInput.get(source));
        return fullInput;
    }
}
