package com.cyancoder.checkout.model;

public record NotificationDispatchResponse(
        String messageKey,
        String status,
        String channel,
        String recipient
) {
}
