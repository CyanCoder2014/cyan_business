package com.cyancoder.notification.model;

public record NotificationDispatchResponse(
        String messageKey,
        String status,
        String channel,
        String recipient
) {
}
