package com.cyancoder.botadapter.service;

import com.cyancoder.botadapter.api.BotIntegrationRequest;
import com.cyancoder.botadapter.api.WebhookResult;
import com.cyancoder.botadapter.config.AiOrchestratorProperties;
import com.cyancoder.botadapter.domain.BotChannel;
import com.cyancoder.botadapter.domain.BotChannelIntegration;
import com.cyancoder.botadapter.domain.BotChatSessionMapping;
import com.cyancoder.botadapter.domain.BotInboundMessage;
import com.cyancoder.botadapter.domain.BotOutboundMessage;
import com.cyancoder.botadapter.repo.BotChannelIntegrationRepository;
import com.cyancoder.botadapter.repo.BotChatSessionMappingRepository;
import com.cyancoder.botadapter.repo.BotInboundMessageRepository;
import com.cyancoder.botadapter.repo.BotMiniAppBuildRepository;
import com.cyancoder.botadapter.repo.BotOutboundMessageRepository;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BotAdapterServiceTest {
    private final BotChannelIntegrationRepository integrationRepository = mock(BotChannelIntegrationRepository.class);
    private final BotChatSessionMappingRepository mappingRepository = mock(BotChatSessionMappingRepository.class);
    private final BotInboundMessageRepository inboundRepository = mock(BotInboundMessageRepository.class);
    private final BotOutboundMessageRepository outboundRepository = mock(BotOutboundMessageRepository.class);
    private final BotMiniAppBuildRepository miniAppBuildRepository = mock(BotMiniAppBuildRepository.class);
    private final BotWebhookParser parser = new BotWebhookParser();
    private final AiConversationClient aiClient = mock(AiConversationClient.class);
    private final BotProviderClient providerClient = mock(BotProviderClient.class);
    private final BotAdapterService service = new BotAdapterService(
            integrationRepository,
            mappingRepository,
            inboundRepository,
            outboundRepository,
            miniAppBuildRepository,
            parser,
            aiClient,
            providerClient
    );

    @Test
    void handleWebhookCreatesSessionMappingAndForwardsMessage() {
        BotChannelIntegration integration = integration();
        when(integrationRepository.findByChannelAndIntegrationKeyAndActiveTrue(BotChannel.TELEGRAM, "retail-bot"))
                .thenReturn(Optional.of(integration));
        when(inboundRepository.findByChannelAndIntegrationKeyAndExternalMessageId(BotChannel.TELEGRAM, "retail-bot", "100"))
                .thenReturn(Optional.empty());
        when(mappingRepository.findByChannelAndIntegrationKeyAndExternalChatId(BotChannel.TELEGRAM, "retail-bot", "200"))
                .thenReturn(Optional.empty());
        when(aiClient.createSession(eq("TELEGRAM"), eq("tenant-demo"), eq("site-demo"), eq("client-demo"), eq("MIXED_BUSINESS_APP"), any()))
                .thenReturn("session-1");
        when(mappingRepository.save(any(BotChatSessionMapping.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inboundRepository.save(any(BotInboundMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WebhookResult result = service.handleWebhook("telegram", "retail-bot", Map.of(
                "message", Map.of(
                        "message_id", 100,
                        "chat", Map.of("id", 200),
                        "text", "Build my shop bot"
                )
        ));

        assertEquals("ACCEPTED", result.status());
        assertEquals("session-1", result.sessionId());
        verify(aiClient).appendUserMessage(eq("session-1"), eq("Build my shop bot"), any());
    }

    @Test
    void handleWebhookIgnoresDuplicateMessage() {
        BotChannelIntegration integration = integration();
        BotInboundMessage inbound = new BotInboundMessage();
        inbound.setSessionId("session-1");
        inbound.setExternalMessageId("100");
        inbound.setExternalChatId("200");
        when(integrationRepository.findByChannelAndIntegrationKeyAndActiveTrue(BotChannel.TELEGRAM, "retail-bot"))
                .thenReturn(Optional.of(integration));
        when(inboundRepository.findByChannelAndIntegrationKeyAndExternalMessageId(BotChannel.TELEGRAM, "retail-bot", "100"))
                .thenReturn(Optional.of(inbound));

        WebhookResult result = service.handleWebhook("telegram", "retail-bot", Map.of(
                "message", Map.of(
                        "message_id", 100,
                        "chat", Map.of("id", 200),
                        "text", "Duplicate"
                )
        ));

        assertEquals("DUPLICATE", result.status());
        verify(aiClient, never()).appendUserMessage(any(), any(), any());
    }

    @Test
    void upsertIntegrationDoesNotExposeTokenValue() {
        when(integrationRepository.findByChannelAndIntegrationKeyAndActiveTrue(BotChannel.BALE, "support-bot"))
                .thenReturn(Optional.empty());
        when(integrationRepository.save(any(BotChannelIntegration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BotChannelIntegration integration = service.upsertIntegration(new BotIntegrationRequest(
                "bale",
                "support-bot",
                "tenant-demo",
                "site-demo",
                "client-demo",
                "CRM",
                "123456789",
                "support",
                "123456789:test-token",
                "vault://bot/support",
                "webhook-secret",
                "https://example.com/miniapp",
                true,
                "tenant-demo",
                true
        ));

        assertEquals("vault://bot/support", integration.getTokenSecretRef());
        assertEquals("123456789", integration.getBotId());
        assertEquals("https://example.com/miniapp", integration.getMiniAppUrl());
        assertEquals(BotChannel.BALE, integration.getChannel());
    }

    private BotChannelIntegration integration() {
        BotChannelIntegration integration = new BotChannelIntegration();
        integration.setChannel(BotChannel.TELEGRAM);
        integration.setIntegrationKey("retail-bot");
        integration.setTenantKey("tenant-demo");
        integration.setSiteKey("site-demo");
        integration.setClientKey("client-demo");
        integration.setAppTypeHint("MIXED_BUSINESS_APP");
        integration.setActive(true);
        integration.setManagedBotToken("123:test-token");
        return integration;
    }

    @Test
    void sendOutboundMessageUsesProviderClient() {
        BotChannelIntegration integration = integration();
        when(integrationRepository.findByChannelAndIntegrationKeyAndActiveTrue(BotChannel.TELEGRAM, "retail-bot"))
                .thenReturn(Optional.of(integration));
        when(outboundRepository.save(any(BotOutboundMessage.class))).thenAnswer(invocation -> {
            BotOutboundMessage message = invocation.getArgument(0);
            if (message.getId() == null) {
                message.setId("outbound-1");
            }
            return message;
        });
        when(providerClient.sendMessage(integration, "200", "Hello from panel"))
                .thenReturn(Map.of("ok", true));

        var result = service.sendOutboundMessage(new com.cyancoder.botadapter.api.OutboundMessageRequest(
                "telegram",
                "retail-bot",
                "200",
                "Hello from panel"
        ));

        assertEquals("SENT", result.status());
        assertEquals("outbound-1", result.deliveryId());
        assertEquals(1, result.attemptCount());
        verify(providerClient).sendMessage(integration, "200", "Hello from panel");
    }

    @Test
    void retryOutboundMessageReusesStoredDelivery() {
        BotChannelIntegration integration = integration();
        BotOutboundMessage message = new BotOutboundMessage();
        message.setId("outbound-2");
        message.setChannel(BotChannel.TELEGRAM);
        message.setIntegrationKey("retail-bot");
        message.setTenantKey("tenant-demo");
        message.setSiteKey("site-demo");
        message.setExternalChatId("200");
        message.setText("Retry me");
        message.setAttemptCount(1);

        when(outboundRepository.findById("outbound-2")).thenReturn(Optional.of(message));
        when(integrationRepository.findByChannelAndIntegrationKeyAndActiveTrue(BotChannel.TELEGRAM, "retail-bot"))
                .thenReturn(Optional.of(integration));
        when(outboundRepository.save(any(BotOutboundMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(providerClient.sendMessage(integration, "200", "Retry me")).thenReturn(Map.of("ok", true));

        var result = service.retryOutboundMessage("outbound-2");

        assertEquals("SENT", result.status());
        assertEquals("outbound-2", result.deliveryId());
        assertEquals(2, result.attemptCount());
    }

    @Test
    void tokenSecretResolverSupportsInlineVaultMapping() {
        AiOrchestratorProperties properties = new AiOrchestratorProperties();
        properties.getBotSecretValues().put("vault://bot/support", "secret-token-123");
        BotTokenSecretResolver resolver = new BotTokenSecretResolver(properties);
        BotChannelIntegration integration = new BotChannelIntegration();
        integration.setIntegrationKey("support-bot");
        integration.setTokenSecretRef("vault://bot/support");

        assertEquals("secret-token-123", resolver.resolveToken(integration));
    }

    @Test
    void tokenSecretResolverFallsBackToManagedTokenForCompatibility() {
        BotTokenSecretResolver resolver = new BotTokenSecretResolver(new AiOrchestratorProperties());
        BotChannelIntegration integration = new BotChannelIntegration();
        integration.setIntegrationKey("legacy-bot");
        integration.setManagedBotToken("legacy-token");

        assertEquals("legacy-token", resolver.resolveToken(integration));
    }
}
