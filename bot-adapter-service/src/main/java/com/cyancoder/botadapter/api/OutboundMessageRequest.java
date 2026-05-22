package com.cyancoder.botadapter.api;

public record OutboundMessageRequest(
        String channel,
        String integrationKey,
        String externalChatId,
        String text
) {
}
