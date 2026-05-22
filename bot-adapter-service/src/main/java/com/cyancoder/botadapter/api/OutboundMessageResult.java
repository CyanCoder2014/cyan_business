package com.cyancoder.botadapter.api;

public record OutboundMessageResult(
        String status,
        String provider,
        String externalChatId,
        String messageText,
        String deliveryId,
        int attemptCount
) {
}
