package com.cyancoder.dynamiccore.expression;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ExpressionParser {

    private final List<ExpressionToken> tokens;
    private final Map<String, Object> context;
    private int current;

    public ExpressionParser(List<ExpressionToken> tokens, Map<String, Object> context) {
        this.tokens = tokens;
        this.context = context;
    }

    public boolean parseExpression() {
        Object result = parseOr();
        expect(ExpressionTokenType.EOF);
        return asBoolean(result);
    }

    private Object parseOr() {
        Object left = parseAnd();
        while (match(ExpressionTokenType.OR)) {
            Object right = parseAnd();
            left = asBoolean(left) || asBoolean(right);
        }
        return left;
    }

    private Object parseAnd() {
        Object left = parseEquality();
        while (match(ExpressionTokenType.AND)) {
            Object right = parseEquality();
            left = asBoolean(left) && asBoolean(right);
        }
        return left;
    }

    private Object parseEquality() {
        Object left = parseComparison();
        while (true) {
            if (match(ExpressionTokenType.EQ)) {
                left = equalsValue(left, parseComparison());
                continue;
            }
            if (match(ExpressionTokenType.NEQ)) {
                left = !equalsValue(left, parseComparison());
                continue;
            }
            return left;
        }
    }

    private Object parseComparison() {
        Object left = parseUnary();
        while (true) {
            if (match(ExpressionTokenType.GT)) {
                left = compare(left, parseUnary()) > 0;
                continue;
            }
            if (match(ExpressionTokenType.GTE)) {
                left = compare(left, parseUnary()) >= 0;
                continue;
            }
            if (match(ExpressionTokenType.LT)) {
                left = compare(left, parseUnary()) < 0;
                continue;
            }
            if (match(ExpressionTokenType.LTE)) {
                left = compare(left, parseUnary()) <= 0;
                continue;
            }
            return left;
        }
    }

    private Object parseUnary() {
        if (match(ExpressionTokenType.NOT)) {
            return !asBoolean(parseUnary());
        }
        return parsePrimary();
    }

    @SuppressWarnings("unchecked")
    private Object parsePrimary() {
        ExpressionToken token = advance();
        return switch (token.type()) {
            case BOOLEAN -> Boolean.parseBoolean(token.text());
            case NULL -> null;
            case STRING -> token.text();
            case NUMBER -> new BigDecimal(token.text());
            case IDENTIFIER -> resolveIdentifier(token.text(), context);
            case LPAREN -> {
                Object result = parseOr();
                expect(ExpressionTokenType.RPAREN);
                yield result;
            }
            default -> throw new IllegalArgumentException("unexpected token: " + token.type());
        };
    }

    private Object resolveIdentifier(String path, Map<String, Object> source) {
        Object currentValue = source;
        for (String part : path.split("\\.")) {
            if (currentValue instanceof Map<?, ?> map) {
                currentValue = map.get(part);
            } else {
                return null;
            }
        }
        return currentValue;
    }

    private int compare(Object left, Object right) {
        if (left == null || right == null) {
            return left == right ? 0 : (left == null ? -1 : 1);
        }
        if (left instanceof Number || right instanceof Number) {
            return new BigDecimal(String.valueOf(left)).compareTo(new BigDecimal(String.valueOf(right)));
        }
        return String.valueOf(left).compareTo(String.valueOf(right));
    }

    private boolean equalsValue(Object left, Object right) {
        if (left == null || right == null) {
            return left == right;
        }
        if (left instanceof Number || right instanceof Number) {
            return new BigDecimal(String.valueOf(left)).compareTo(new BigDecimal(String.valueOf(right))) == 0;
        }
        return String.valueOf(left).equals(String.valueOf(right));
    }

    private boolean asBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private boolean match(ExpressionTokenType type) {
        if (check(type)) {
            current++;
            return true;
        }
        return false;
    }

    private void expect(ExpressionTokenType type) {
        if (!match(type)) {
            throw new IllegalArgumentException("expected token: " + type);
        }
    }

    private boolean check(ExpressionTokenType type) {
        return peek().type() == type;
    }

    private ExpressionToken advance() {
        return tokens.get(current++);
    }

    private ExpressionToken peek() {
        return tokens.get(current);
    }
}
