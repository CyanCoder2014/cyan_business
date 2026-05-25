package com.cyancoder.botadapter.api;

public record WebhookRegistrationResult(
        String status,
        String channel,
        String integrationKey,
        String webhookUrl
) {
}
