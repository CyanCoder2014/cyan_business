package com.cyancoder.botadapter.api;

public record WebhookResult(
        String status,
        String sessionId,
        String externalMessageId,
        String externalChatId
) {
}
