package com.cyancoder.botadapter.api;

public record BotIntegrationRequest(
        String channel,
        String integrationKey,
        String tenantKey,
        String siteKey,
        String clientKey,
        String appTypeHint,
        String botId,
        String botUsername,
        String botToken,
        String tokenSecretRef,
        String webhookSecret,
        String webhookSecretRef,
        String miniAppUrl,
        Boolean miniAppEnabled,
        String miniAppStartParam,
        Boolean active
) {
}
