package com.cyancoder.botadapter.api;

public record RetryOutboundMessageResult(
        String status,
        String deliveryId,
        int attemptCount
) {
}
