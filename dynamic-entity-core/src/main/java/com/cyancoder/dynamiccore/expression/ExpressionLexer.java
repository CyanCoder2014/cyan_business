package com.cyancoder.dynamiccore.expression;

import java.util.ArrayList;
import java.util.List;

public class ExpressionLexer {

    private final String input;
    private int position;

    public ExpressionLexer(String input) {
        this.input = input == null ? "" : input;
    }

    public List<ExpressionToken> tokenize() {
        List<ExpressionToken> tokens = new ArrayList<>();
        while (!isAtEnd()) {
            char c = peek();
            if (Character.isWhitespace(c)) {
                advance();
                continue;
            }
            if (c == '(') {
                tokens.add(new ExpressionToken(ExpressionTokenType.LPAREN, "("));
                advance();
                continue;
            }
            if (c == ')') {
                tokens.add(new ExpressionToken(ExpressionTokenType.RPAREN, ")"));
                advance();
                continue;
            }
            if (c == '!' && peekNext() == '=') {
                tokens.add(new ExpressionToken(ExpressionTokenType.NEQ, "!="));
                advance();
                advance();
                continue;
            }
            if (c == '=' && peekNext() == '=') {
                tokens.add(new ExpressionToken(ExpressionTokenType.EQ, "=="));
                advance();
                advance();
                continue;
            }
            if (c == '>' && peekNext() == '=') {
                tokens.add(new ExpressionToken(ExpressionTokenType.GTE, ">="));
                advance();
                advance();
                continue;
            }
            if (c == '<' && peekNext() == '=') {
                tokens.add(new ExpressionToken(ExpressionTokenType.LTE, "<="));
                advance();
                advance();
                continue;
            }
            if (c == '>') {
                tokens.add(new ExpressionToken(ExpressionTokenType.GT, ">"));
                advance();
                continue;
            }
            if (c == '<') {
                tokens.add(new ExpressionToken(ExpressionTokenType.LT, "<"));
                advance();
                continue;
            }
            if (c == '&' && peekNext() == '&') {
                tokens.add(new ExpressionToken(ExpressionTokenType.AND, "&&"));
                advance();
                advance();
                continue;
            }
            if (c == '|' && peekNext() == '|') {
                tokens.add(new ExpressionToken(ExpressionTokenType.OR, "||"));
                advance();
                advance();
                continue;
            }
            if (c == '!') {
                tokens.add(new ExpressionToken(ExpressionTokenType.NOT, "!"));
                advance();
                continue;
            }
            if (c == '"' || c == '\'') {
                tokens.add(readString());
                continue;
            }
            if (Character.isDigit(c) || (c == '-' && Character.isDigit(peekNext()))) {
                tokens.add(readNumber());
                continue;
            }
            if (Character.isLetter(c) || c == '_') {
                tokens.add(readIdentifierOrKeyword());
                continue;
            }
            throw new IllegalArgumentException("unexpected character in expression: " + c);
        }
        tokens.add(new ExpressionToken(ExpressionTokenType.EOF, ""));
        return tokens;
    }

    private ExpressionToken readString() {
        char quote = advance();
        StringBuilder builder = new StringBuilder();
        while (!isAtEnd() && peek() != quote) {
            char c = advance();
            if (c == '\\' && !isAtEnd()) {
                char next = advance();
                builder.append(next);
            } else {
                builder.append(c);
            }
        }
        if (isAtEnd()) {
            throw new IllegalArgumentException("unterminated string");
        }
        advance();
        return new ExpressionToken(ExpressionTokenType.STRING, builder.toString());
    }

    private ExpressionToken readNumber() {
        StringBuilder builder = new StringBuilder();
        if (peek() == '-') {
            builder.append(advance());
        }
        while (!isAtEnd() && (Character.isDigit(peek()) || peek() == '.')) {
            builder.append(advance());
        }
        return new ExpressionToken(ExpressionTokenType.NUMBER, builder.toString());
    }

    private ExpressionToken readIdentifierOrKeyword() {
        StringBuilder builder = new StringBuilder();
        while (!isAtEnd() && (Character.isLetterOrDigit(peek()) || peek() == '_' || peek() == '.')) {
            builder.append(advance());
        }
        String text = builder.toString();
        if ("true".equalsIgnoreCase(text) || "false".equalsIgnoreCase(text)) {
            return new ExpressionToken(ExpressionTokenType.BOOLEAN, text);
        }
        if ("null".equalsIgnoreCase(text)) {
            return new ExpressionToken(ExpressionTokenType.NULL, text);
        }
        return new ExpressionToken(ExpressionTokenType.IDENTIFIER, text);
    }

    private char advance() {
        return input.charAt(position++);
    }

    private char peek() {
        return input.charAt(position);
    }

    private char peekNext() {
        return position + 1 >= input.length() ? '\0' : input.charAt(position + 1);
    }

    private boolean isAtEnd() {
        return position >= input.length();
    }
}
