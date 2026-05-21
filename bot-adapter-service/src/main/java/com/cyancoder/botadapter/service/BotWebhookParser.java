package com.cyancoder.botadapter.service;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BotWebhookParser {
    public ParsedBotMessage parse(Map<String, Object> payload) {
        Map<String, Object> message = map(payload.get("message"));
        if (message.isEmpty()) {
            message = map(payload.get("edited_message"));
        }
        if (message.isEmpty()) {
            message = payload;
        }

        String messageId = stringValue(firstNonNull(
                message.get("message_id"),
                message.get("messageId"),
                payload.get("update_id"),
                payload.get("updateId")
        ));
        Map<String, Object> chat = map(message.get("chat"));
        String chatId = stringValue(firstNonNull(
                chat.get("id"),
                message.get("chat_id"),
                message.get("chatId"),
                payload.get("chat_id"),
                payload.get("chatId")
        ));
        String text = stringValue(firstNonNull(
                message.get("text"),
                message.get("caption"),
                payload.get("text")
        ));

        if (messageId == null || messageId.isBlank()) {
            messageId = chatId + ":" + Integer.toHexString(payload.hashCode());
        }
        if (chatId == null || chatId.isBlank()) {
            throw new IllegalArgumentException("Webhook payload does not contain chat id");
        }
        if (text == null || text.isBlank()) {
            text = "";
        }
        return new ParsedBotMessage(messageId, chatId, text);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> ? (Map<String, Object>) value : Map.of();
    }

    private Object firstNonNull(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    public record ParsedBotMessage(String messageId, String chatId, String text) {
    }
}
