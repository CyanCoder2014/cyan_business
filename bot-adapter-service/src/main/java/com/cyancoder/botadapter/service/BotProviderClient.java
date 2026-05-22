package com.cyancoder.botadapter.service;

import com.cyancoder.botadapter.config.AiOrchestratorProperties;
import com.cyancoder.botadapter.domain.BotChannel;
import com.cyancoder.botadapter.domain.BotChannelIntegration;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class BotProviderClient {
    private final RestTemplate restTemplate;
    private final AiOrchestratorProperties properties;

    public BotProviderClient(RestTemplate restTemplate, AiOrchestratorProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public void registerWebhook(BotChannelIntegration integration) {
        String token = resolveToken(integration);
        String baseUrl = baseUrl(integration.getChannel());
        String webhookUrl = properties.getPublicBaseUrl().replaceAll("/$", "")
                + "/public/bot-adapter/"
                + integration.getChannel().name().toLowerCase()
                + "/"
                + integration.getIntegrationKey()
                + "/webhook";
        restTemplate.postForObject(
                baseUrl + "/bot" + token + "/setWebhook",
                Map.of(
                        "url", webhookUrl,
                        "secret_token", integration.getWebhookSecret() == null ? "" : integration.getWebhookSecret()
                ),
                Map.class
        );
    }

    public void sendMessage(BotChannelIntegration integration, String externalChatId, String text) {
        String token = resolveToken(integration);
        String baseUrl = baseUrl(integration.getChannel());
        restTemplate.postForObject(
                baseUrl + "/bot" + token + "/sendMessage",
                Map.of(
                        "chat_id", externalChatId,
                        "text", text
                ),
                Map.class
        );
    }

    private String baseUrl(BotChannel channel) {
        return channel == BotChannel.BALE ? properties.getBaleBaseUrl() : properties.getTelegramBaseUrl();
    }

    private String resolveToken(BotChannelIntegration integration) {
        if (integration.getManagedBotToken() == null || integration.getManagedBotToken().isBlank()) {
            throw new IllegalStateException("No managed bot token is stored for integration " + integration.getIntegrationKey());
        }
        return integration.getManagedBotToken();
    }
}
