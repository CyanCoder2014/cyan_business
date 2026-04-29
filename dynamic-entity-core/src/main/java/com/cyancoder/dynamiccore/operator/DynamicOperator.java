package com.cyancoder.dynamiccore.operator;

import com.cyancoder.dynamiccore.model.OperationRule;

import java.util.Map;

public interface DynamicOperator {
    String name();

    Map<String, Object> apply(OperationRule rule, String path, Object value, Map<String, Object> currentObject, Map<String, Object> fullInput);
}
