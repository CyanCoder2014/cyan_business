package com.cyancoder.botadapter.service;

import com.cyancoder.botadapter.config.AiOrchestratorProperties;
import com.cyancoder.botadapter.domain.BotChannel;
import com.cyancoder.botadapter.domain.BotChannelIntegration;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@Component
public class BotProviderClient {
    private final RestTemplate restTemplate;
    private final AiOrchestratorProperties properties;
    private final BotTokenSecretResolver botTokenSecretResolver;

    public BotProviderClient(RestTemplate restTemplate, AiOrchestratorProperties properties, BotTokenSecretResolver botTokenSecretResolver) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.botTokenSecretResolver = botTokenSecretResolver;
    }

    public Map<String, Object> registerWebhook(BotChannelIntegration integration) {
        String token = resolveToken(integration);
        String baseUrl = baseUrl(integration.getChannel());
        String webhookUrl = webhookUrl(integration);
        String webhookSecret = botTokenSecretResolver.resolveReference(integration.getWebhookSecretRef(), "webhook");
        Object response = restTemplate.postForObject(
                baseUrl + "/bot" + token + "/setWebhook",
                Map.of(
                        "url", webhookUrl,
                        "secret_token", webhookSecret
                ),
                Map.class
        );
        return asMap(response);
    }

    public String webhookUrl(BotChannelIntegration integration) {
        if (properties.getPublicBaseUrl() == null || properties.getPublicBaseUrl().isBlank()) {
            throw new IllegalStateException("NOT_CONFIGURED: public bot webhook base URL is required");
        }
        return properties.getPublicBaseUrl().replaceAll("/$", "")
                + "/public/bot-adapter/"
                + integration.getChannel().name().toLowerCase()
                + "/"
                + integration.getIntegrationKey()
                + "/webhook";
    }

    public void verifyWebhookSecret(BotChannelIntegration integration, String suppliedSecret) {
        String expected = botTokenSecretResolver.resolveReference(integration.getWebhookSecretRef(), "webhook");
        if (suppliedSecret == null || !MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), suppliedSecret.getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("Invalid webhook signature");
        }
    }

    public Map<String, Object> sendMessage(BotChannelIntegration integration, String externalChatId, String text) {
        String token = resolveToken(integration);
        String baseUrl = baseUrl(integration.getChannel());
        Object response = restTemplate.postForObject(
                baseUrl + "/bot" + token + "/sendMessage",
                Map.of(
                        "chat_id", externalChatId,
                        "text", text
                ),
                Map.class
        );
        return asMap(response);
    }

    private String baseUrl(BotChannel channel) {
        return channel == BotChannel.BALE ? properties.getBaleBaseUrl() : properties.getTelegramBaseUrl();
    }

    private String resolveToken(BotChannelIntegration integration) {
        return botTokenSecretResolver.resolveToken(integration);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }
}
