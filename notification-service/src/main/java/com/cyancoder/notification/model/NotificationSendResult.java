package com.cyancoder.notification.model;

public record NotificationSendResult(
        boolean successful,
        String provider,
        String providerMessageId,
        String status,
        String errorMessage
) {
}
