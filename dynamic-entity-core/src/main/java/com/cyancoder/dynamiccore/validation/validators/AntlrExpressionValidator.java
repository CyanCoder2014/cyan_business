package com.cyancoder.dynamiccore.validation.validators;

import com.cyancoder.dynamiccore.expression.ExpressionLexer;
import com.cyancoder.dynamiccore.expression.ExpressionParser;
import com.cyancoder.dynamiccore.model.ValidationRule;
import com.cyancoder.dynamiccore.validation.DynamicValidator;
import com.cyancoder.dynamiccore.validation.ValidatorResponse;

import java.util.List;
import java.util.Map;

public class AntlrExpressionValidator implements DynamicValidator {
    @Override
    public String name() {
        return "AntlrExpression";
    }

    @Override
    public ValidatorResponse validate(
            ValidationRule rule,
            String path,
            Object value,
            Map<String, Object> currentObject,
            Map<String, Object> fullInput,
            Map<String, Object> params,
            String serviceKey,
            String entityKey
    ) {
        try {
            String expression = String.valueOf(params.getOrDefault("expression", ""));
            boolean isRoot = Boolean.parseBoolean(String.valueOf(params.getOrDefault("isRoot", "false")));
            Map<String, Object> context = isRoot ? fullInput : currentObject;
            boolean valid = new ExpressionParser(new ExpressionLexer(expression).tokenize(), context).parseExpression();
            return new ValidatorResponse(valid, fullInput, valid ? null : rule.getValidationMessage());
        } catch (Exception ex) {
            return new ValidatorResponse(false, fullInput, ex.getMessage());
        }
    }
}
