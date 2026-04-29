package com.cyancoder.dynamiccore.operator.operators;

import com.cyancoder.dynamiccore.model.OperationRule;
import com.cyancoder.dynamiccore.operator.DynamicOperator;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

public class SumFieldsOperator implements DynamicOperator {
    @Override
    public String name() {
        return "SUM_FIELDS";
    }

    @Override
    public Map<String, Object> apply(OperationRule rule, String path, Object value, Map<String, Object> currentObject, Map<String, Object> fullInput) {
        Object raw = rule.getOperationParams().get("sourceFields");
        BigDecimal total = BigDecimal.ZERO;
        if (raw instanceof Collection<?> collection) {
            for (Object field : collection) {
                Object fieldValue = fullInput.get(String.valueOf(field));
                if (fieldValue != null) {
                    total = total.add(new BigDecimal(String.valueOf(fieldValue)));
                }
            }
        }
        String target = String.valueOf(rule.getOperationParams().get("targetField"));
        fullInput.put(target, total);
        return fullInput;
    }
}
