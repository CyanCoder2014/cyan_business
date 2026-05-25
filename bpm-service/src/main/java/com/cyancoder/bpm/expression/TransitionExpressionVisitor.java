package com.cyancoder.bpm.expression;

import com.cyancoder.bpm.expression.parser.TransitionExpressionBaseVisitor;
import com.cyancoder.bpm.expression.parser.TransitionExpressionParser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

class TransitionExpressionVisitor extends TransitionExpressionBaseVisitor<Object> {
    private final Map<String, Object> variables;

    TransitionExpressionVisitor(Map<String, Object> variables) {
        this.variables = variables;
    }

    @Override
    public Object visitExpr(TransitionExpressionParser.ExprContext ctx) {
        return visit(ctx.logicalOr());
    }

    @Override
    public Object visitLogicalOr(TransitionExpressionParser.LogicalOrContext ctx) {
        boolean result = toBoolean(visit(ctx.logicalAnd(0)));
        for (int i = 1; i < ctx.logicalAnd().size(); i++) {
            if (result) {
                return true;
            }
            result = result || toBoolean(visit(ctx.logicalAnd(i)));
        }
        return result;
    }

    @Override
    public Object visitLogicalAnd(TransitionExpressionParser.LogicalAndContext ctx) {
        boolean result = toBoolean(visit(ctx.equality(0)));
        for (int i = 1; i < ctx.equality().size(); i++) {
            if (!result) {
                return false;
            }
            result = result && toBoolean(visit(ctx.equality(i)));
        }
        return result;
    }

    @Override
    public Object visitEquality(TransitionExpressionParser.EqualityContext ctx) {
        if (ctx.relational().size() == 1) {
            return visit(ctx.relational(0));
        }
        Object left = visit(ctx.relational(0));
        for (int i = 1; i < ctx.relational().size(); i++) {
            Object right = visit(ctx.relational(i));
            String op = ctx.getChild(2 * i - 1).getText();
            boolean equal = areEqual(left, right);
            if ("==".equals(op) && !equal) {
                return false;
            }
            if ("!=".equals(op) && equal) {
                return false;
            }
            left = right;
        }
        return true;
    }

    @Override
    public Object visitRelational(TransitionExpressionParser.RelationalContext ctx) {
        if ((ctx.GT() != null && !ctx.GT().isEmpty())
                || (ctx.GE() != null && !ctx.GE().isEmpty())
                || (ctx.LT() != null && !ctx.LT().isEmpty())
                || (ctx.LE() != null && !ctx.LE().isEmpty())) {
            Object left = visit(ctx.addition(0));
            for (int i = 1; i < ctx.addition().size(); i++) {
                Object right = visit(ctx.addition(i));
                String op = ctx.getChild(2 * i - 1).getText();
                double l = toDouble(left);
                double r = toDouble(right);
                switch (op) {
                    case ">" -> { if (!(l > r)) { return false; } }
                    case ">=" -> { if (!(l >= r)) { return false; } }
                    case "<" -> { if (!(l < r)) { return false; } }
                    case "<=" -> { if (!(l <= r)) { return false; } }
                    default -> throw new IllegalArgumentException("Unsupported operator: " + op);
                }
                left = right;
            }
            return true;
        }
        if (ctx.CONTAINS() != null || ctx.NOTCONTAINS() != null) {
            Object left = visit(ctx.addition(0));
            List<?> list = (List<?>) visit(ctx.list());
            boolean result;
            if (left instanceof Collection<?> collection) {
                result = list.stream().anyMatch(item -> collection.stream().anyMatch(entry -> areEqual(entry, item)));
            } else if (left != null && left.getClass().isArray()) {
                result = false;
                for (Object item : list) {
                    for (int i = 0; i < Array.getLength(left); i++) {
                        if (areEqual(Array.get(left, i), item)) {
                            result = true;
                            break;
                        }
                    }
                    if (result) {
                        break;
                    }
                }
            } else {
                result = list.stream().anyMatch(item -> areEqual(item, left));
            }
            return ctx.NOTCONTAINS() != null ? !result : result;
        }
        if (ctx.BETWEEN() != null) {
            double value = toDouble(visit(ctx.addition(0)));
            double start = toDouble(visit(ctx.addition(1)));
            double end = toDouble(visit(ctx.addition(2)));
            return value >= start && value <= end;
        }
        if (ctx.IS() != null) {
            return visit(ctx.addition(0)) == null;
        }
        if (ctx.EMPTY() != null) {
            boolean result = isEmpty(visit(ctx.addition(0)));
            return ctx.NOT() != null ? !result : result;
        }
        if (ctx.STARTSWITH() != null || ctx.ENDSWITH() != null || ctx.CONTAINSSTR() != null || ctx.MATCHES() != null) {
            String left = asString(visit(ctx.addition(0)));
            String right = asString(visit(ctx.addition(1)));
            if (ctx.STARTSWITH() != null) {
                return left.startsWith(right);
            }
            if (ctx.ENDSWITH() != null) {
                return left.endsWith(right);
            }
            if (ctx.CONTAINSSTR() != null) {
                return left.contains(right);
            }
            return Pattern.compile(right).matcher(left).find();
        }
        return visit(ctx.addition(0));
    }

    @Override
    public Object visitAddition(TransitionExpressionParser.AdditionContext ctx) {
        Object current = visit(ctx.multiplication(0));
        if (!(current instanceof Number) && !isNumericString(current)) {
            return current;
        }
        double result = toDouble(current);
        for (int i = 1; i < ctx.multiplication().size(); i++) {
            double next = toDouble(visit(ctx.multiplication(i)));
            String op = ctx.getChild(2 * i - 1).getText();
            result = "+".equals(op) ? result + next : result - next;
        }
        return result;
    }

    @Override
    public Object visitMultiplication(TransitionExpressionParser.MultiplicationContext ctx) {
        Object current = visit(ctx.atom(0));
        if (!(current instanceof Number) && !isNumericString(current)) {
            return current;
        }
        double result = toDouble(current);
        for (int i = 1; i < ctx.atom().size(); i++) {
            double next = toDouble(visit(ctx.atom(i)));
            String op = ctx.getChild(2 * i - 1).getText();
            result = "*".equals(op) ? result * next : result / next;
        }
        return result;
    }

    @Override
    public Object visitAtom(TransitionExpressionParser.AtomContext ctx) {
        if (ctx.NUMBER() != null) {
            return Double.parseDouble(ctx.NUMBER().getText());
        }
        if (ctx.STRING() != null) {
            return ctx.STRING().getText().replaceAll("^\"|\"$", "");
        }
        if (ctx.BOOLEAN() != null) {
            return Boolean.parseBoolean(ctx.BOOLEAN().getText());
        }
        if (ctx.NULL() != null) {
            return null;
        }
        if (ctx.dottedIdentifier() != null) {
            return visit(ctx.dottedIdentifier());
        }
        if (ctx.list() != null) {
            return visit(ctx.list());
        }
        if (ctx.expr() != null) {
            return visit(ctx.expr());
        }
        return null;
    }

    @Override
    public Object visitDottedIdentifier(TransitionExpressionParser.DottedIdentifierContext ctx) {
        Object current = variables;
        for (var id : ctx.IDENTIFIER()) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = map.get(id.getText());
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    @Override
    public Object visitList(TransitionExpressionParser.ListContext ctx) {
        List<Object> values = new ArrayList<>();
        if (ctx.elements() == null) {
            return values;
        }
        for (var atom : ctx.elements().atom()) {
            values.add(visit(atom));
        }
        return values;
    }

    private boolean areEqual(Object left, Object right) {
        if (left == null || right == null) {
            return left == right;
        }
        if (isNumericString(left) || left instanceof Number) {
            if (isNumericString(right) || right instanceof Number) {
                return Double.compare(toDouble(left), toDouble(right)) == 0;
            }
        }
        return Objects.equals(normalize(left), normalize(right));
    }

    private Object normalize(Object value) {
        return value instanceof String string ? string.trim() : value;
    }

    private String asString(Object value) {
        return value == null ? "" : value.toString();
    }

    private boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String string) {
            return string.isBlank();
        }
        if (value instanceof List<?> list) {
            return list.isEmpty();
        }
        if (value instanceof Map<?, ?> map) {
            return map.isEmpty();
        }
        return value.getClass().isArray() && Array.getLength(value) == 0;
    }

    private boolean toBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.doubleValue() != 0d;
        }
        if (value instanceof String string) {
            return !string.isBlank();
        }
        return value != null;
    }

    private double toDouble(Object value) {
        if (value == null) {
            return 0d;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(value.toString());
    }

    private boolean isNumericString(Object value) {
        if (!(value instanceof String string)) {
            return false;
        }
        try {
            Double.parseDouble(string);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}

