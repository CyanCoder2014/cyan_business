package com.cyancoder.botadapter.service;

import com.cyancoder.botadapter.api.BotIntegrationRequest;
import com.cyancoder.botadapter.api.WebhookResult;
import com.cyancoder.botadapter.domain.BotChannel;
import com.cyancoder.botadapter.domain.BotChannelIntegration;
import com.cyancoder.botadapter.domain.BotChatSessionMapping;
import com.cyancoder.botadapter.domain.BotInboundMessage;
import com.cyancoder.botadapter.repo.BotChannelIntegrationRepository;
import com.cyancoder.botadapter.repo.BotChatSessionMappingRepository;
import com.cyancoder.botadapter.repo.BotInboundMessageRepository;
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
    private final BotWebhookParser parser = new BotWebhookParser();
    private final AiConversationClient aiClient = mock(AiConversationClient.class);
    private final BotAdapterService service = new BotAdapterService(
            integrationRepository,
            mappingRepository,
            inboundRepository,
            parser,
            aiClient
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
        return integration;
    }
}
