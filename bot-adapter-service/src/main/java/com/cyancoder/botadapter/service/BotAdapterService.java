package com.cyancoder.botadapter.service;

import com.cyancoder.botadapter.api.BotIntegrationRequest;
import com.cyancoder.botadapter.api.OutboundMessageRequest;
import com.cyancoder.botadapter.api.OutboundMessageResult;
import com.cyancoder.botadapter.api.RetryOutboundMessageResult;
import com.cyancoder.botadapter.api.WebhookResult;
import com.cyancoder.botadapter.api.WebhookRegistrationResult;
import com.cyancoder.botadapter.domain.BotChannel;
import com.cyancoder.botadapter.domain.BotChannelIntegration;
import com.cyancoder.botadapter.domain.BotChatSessionMapping;
import com.cyancoder.botadapter.domain.BotInboundMessage;
import com.cyancoder.botadapter.domain.BotOutboundMessage;
import com.cyancoder.botadapter.repo.BotChannelIntegrationRepository;
import com.cyancoder.botadapter.repo.BotChatSessionMappingRepository;
import com.cyancoder.botadapter.repo.BotInboundMessageRepository;
import com.cyancoder.botadapter.repo.BotOutboundMessageRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class BotAdapterService {
    private final BotChannelIntegrationRepository integrationRepository;
    private final BotChatSessionMappingRepository mappingRepository;
    private final BotInboundMessageRepository inboundMessageRepository;
    private final BotOutboundMessageRepository outboundMessageRepository;
    private final BotWebhookParser webhookParser;
    private final AiConversationClient aiConversationClient;
    private final BotProviderClient botProviderClient;

    public BotAdapterService(BotChannelIntegrationRepository integrationRepository,
                             BotChatSessionMappingRepository mappingRepository,
                             BotInboundMessageRepository inboundMessageRepository,
                             BotOutboundMessageRepository outboundMessageRepository,
                             BotWebhookParser webhookParser,
                             AiConversationClient aiConversationClient,
                             BotProviderClient botProviderClient) {
        this.integrationRepository = integrationRepository;
        this.mappingRepository = mappingRepository;
        this.inboundMessageRepository = inboundMessageRepository;
        this.outboundMessageRepository = outboundMessageRepository;
        this.webhookParser = webhookParser;
        this.aiConversationClient = aiConversationClient;
        this.botProviderClient = botProviderClient;
    }

    public BotChannelIntegration upsertIntegration(BotIntegrationRequest request) {
        BotChannel channel = parseChannel(request.channel());
        BotChannelIntegration integration = integrationRepository
                .findByChannelAndIntegrationKeyAndActiveTrue(channel, request.integrationKey())
                .orElseGet(BotChannelIntegration::new);
        integration.setChannel(channel);
        integration.setIntegrationKey(required(request.integrationKey(), "integrationKey"));
        integration.setTenantKey(required(request.tenantKey(), "tenantKey"));
        integration.setSiteKey(required(request.siteKey(), "siteKey"));
        integration.setClientKey(request.clientKey());
        integration.setAppTypeHint(request.appTypeHint());
        integration.setBotId(request.botId());
        integration.setBotUsername(request.botUsername());
        if (request.botToken() != null && !request.botToken().isBlank()) {
            integration.setManagedBotToken(request.botToken());
        }
        integration.setTokenSecretRef(resolveTokenSecretRef(request));
        if (request.botToken() != null && !request.botToken().isBlank()) {
            integration.setTokenFingerprint(fingerprint(request.botToken()));
        }
        integration.setWebhookSecret(request.webhookSecret());
        integration.setMiniAppUrl(request.miniAppUrl());
        integration.setMiniAppEnabled(Boolean.TRUE.equals(request.miniAppEnabled()));
        integration.setMiniAppStartParam(request.miniAppStartParam());
        integration.setProviderConfig(providerConfig(request));
        integration.setActive(request.active() == null || request.active());
        if (integration.getCreatedAt() == null) {
            integration.setCreatedAt(Instant.now());
        }
        integration.setUpdatedAt(Instant.now());
        return integrationRepository.save(integration);
    }

    public List<BotChannelIntegration> listIntegrations(String tenantKey, String siteKey) {
        if (tenantKey != null && !tenantKey.isBlank() && siteKey != null && !siteKey.isBlank()) {
            return integrationRepository.findByTenantKeyAndSiteKeyOrderByUpdatedAtDesc(tenantKey, siteKey);
        }
        if (tenantKey != null && !tenantKey.isBlank()) {
            return integrationRepository.findByTenantKeyOrderByUpdatedAtDesc(tenantKey);
        }
        return integrationRepository.findAll();
    }

    public WebhookResult handleWebhook(String channelValue, String integrationKey, Map<String, Object> payload) {
        BotChannel channel = parseChannel(channelValue);
        BotChannelIntegration integration = integrationRepository
                .findByChannelAndIntegrationKeyAndActiveTrue(channel, integrationKey)
                .orElseThrow(() -> new IllegalArgumentException("Active bot integration not found"));
        BotWebhookParser.ParsedBotMessage parsed = webhookParser.parse(payload);

        var existing = inboundMessageRepository.findByChannelAndIntegrationKeyAndExternalMessageId(
                channel,
                integrationKey,
                parsed.messageId()
        );
        if (existing.isPresent()) {
            BotInboundMessage inbound = existing.get();
            return new WebhookResult("DUPLICATE", inbound.getSessionId(), inbound.getExternalMessageId(), inbound.getExternalChatId());
        }

        BotChatSessionMapping mapping = mappingRepository
                .findByChannelAndIntegrationKeyAndExternalChatId(channel, integrationKey, parsed.chatId())
                .orElseGet(() -> createMapping(integration, parsed.chatId()));

        aiConversationClient.appendUserMessage(mapping.getSessionId(), parsed.text(), Map.of(
                "channel", channel.name().toLowerCase(Locale.ROOT),
                "externalChatId", parsed.chatId(),
                "integrationKey", integrationKey
        ));

        BotInboundMessage inbound = new BotInboundMessage();
        inbound.setChannel(channel);
        inbound.setIntegrationKey(integrationKey);
        inbound.setExternalMessageId(parsed.messageId());
        inbound.setExternalChatId(parsed.chatId());
        inbound.setSessionId(mapping.getSessionId());
        inbound.setText(parsed.text());
        inbound.setRawPayload(payload);
        inbound.setStatus("FORWARDED_TO_AI_ORCHESTRATOR");
        inbound.setReceivedAt(Instant.now());
        inboundMessageRepository.save(inbound);
        return new WebhookResult("ACCEPTED", mapping.getSessionId(), parsed.messageId(), parsed.chatId());
    }

    public WebhookRegistrationResult registerWebhook(String channelValue, String integrationKey) {
        BotChannelIntegration integration = findActiveIntegration(channelValue, integrationKey);
        botProviderClient.registerWebhook(integration);
        return new WebhookRegistrationResult(
                "REGISTERED",
                integration.getChannel().name(),
                integration.getIntegrationKey(),
                "public-base-url-configured/webhook"
        );
    }

    public OutboundMessageResult sendOutboundMessage(OutboundMessageRequest request) {
        BotChannelIntegration integration = findActiveIntegration(request.channel(), request.integrationKey());
        BotOutboundMessage message = new BotOutboundMessage();
        message.setChannel(integration.getChannel());
        message.setIntegrationKey(integration.getIntegrationKey());
        message.setTenantKey(integration.getTenantKey());
        message.setSiteKey(integration.getSiteKey());
        message.setClientKey(integration.getClientKey());
        message.setExternalChatId(required(request.externalChatId(), "externalChatId"));
        message.setText(required(request.text(), "text"));
        message.setStatus("PENDING");
        message.setCreatedAt(Instant.now());
        message.setUpdatedAt(Instant.now());
        message = outboundMessageRepository.save(message);
        return dispatchOutboundMessage(integration, message);
    }

    public List<BotOutboundMessage> listOutboundMessages(String tenantKey, String siteKey, String integrationKey) {
        if (tenantKey != null && !tenantKey.isBlank() && siteKey != null && !siteKey.isBlank()) {
            return outboundMessageRepository.findByTenantKeyAndSiteKeyOrderByUpdatedAtDesc(tenantKey, siteKey);
        }
        if (tenantKey != null && !tenantKey.isBlank()) {
            return outboundMessageRepository.findByTenantKeyOrderByUpdatedAtDesc(tenantKey);
        }
        if (integrationKey != null && !integrationKey.isBlank()) {
            return outboundMessageRepository.findByIntegrationKeyOrderByUpdatedAtDesc(integrationKey);
        }
        return outboundMessageRepository.findAll();
    }

    public RetryOutboundMessageResult retryOutboundMessage(String messageId) {
        BotOutboundMessage message = outboundMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Outbound message not found"));
        BotChannelIntegration integration = findActiveIntegration(message.getChannel().name(), message.getIntegrationKey());
        OutboundMessageResult result = dispatchOutboundMessage(integration, message);
        return new RetryOutboundMessageResult(result.status(), result.deliveryId(), result.attemptCount());
    }

    private OutboundMessageResult dispatchOutboundMessage(BotChannelIntegration integration, BotOutboundMessage message) {
        message.setAttemptCount(message.getAttemptCount() + 1);
        message.setLastAttemptAt(Instant.now());
        message.setUpdatedAt(Instant.now());
        try {
            Map<String, Object> providerResponse = botProviderClient.sendMessage(integration, message.getExternalChatId(), message.getText());
            message.setStatus("SENT");
            message.setDeliveredAt(Instant.now());
            message.setErrorMessage(null);
            message.setProviderResponse(providerResponse);
            outboundMessageRepository.save(message);
            return new OutboundMessageResult(
                    "SENT",
                    integration.getChannel().name(),
                    message.getExternalChatId(),
                    message.getText(),
                    message.getId(),
                    message.getAttemptCount()
            );
        } catch (RuntimeException ex) {
            message.setStatus("FAILED");
            message.setErrorMessage(ex.getMessage());
            outboundMessageRepository.save(message);
            throw ex;
        }
    }

    private BotChatSessionMapping createMapping(BotChannelIntegration integration, String externalChatId) {
        String sessionId = aiConversationClient.createSession(
                integration.getChannel().name(),
                integration.getTenantKey(),
                integration.getSiteKey(),
                integration.getClientKey(),
                integration.getAppTypeHint(),
                Map.of(
                        "channel", integration.getChannel().name().toLowerCase(Locale.ROOT),
                        "externalChatId", externalChatId,
                        "integrationKey", integration.getIntegrationKey()
                )
        );
        BotChatSessionMapping mapping = new BotChatSessionMapping();
        mapping.setChannel(integration.getChannel());
        mapping.setIntegrationKey(integration.getIntegrationKey());
        mapping.setExternalChatId(externalChatId);
        mapping.setSessionId(sessionId);
        mapping.setCreatedAt(Instant.now());
        mapping.setUpdatedAt(Instant.now());
        return mappingRepository.save(mapping);
    }

    private BotChannelIntegration findActiveIntegration(String channelValue, String integrationKey) {
        BotChannel channel = parseChannel(channelValue);
        return integrationRepository
                .findByChannelAndIntegrationKeyAndActiveTrue(channel, integrationKey)
                .orElseThrow(() -> new IllegalArgumentException("Active bot integration not found"));
    }

    private BotChannel parseChannel(String value) {
        return BotChannel.valueOf(required(value, "channel").trim().toUpperCase(Locale.ROOT));
    }

    private String resolveTokenSecretRef(BotIntegrationRequest request) {
        if (request.tokenSecretRef() != null && !request.tokenSecretRef().isBlank()) {
            return request.tokenSecretRef();
        }
        if (request.botToken() != null && !request.botToken().isBlank()) {
            return "managed://" + request.channel().toLowerCase(Locale.ROOT) + "/" + request.integrationKey();
        }
        return null;
    }

    private Map<String, Object> providerConfig(BotIntegrationRequest request) {
        Map<String, Object> config = new LinkedHashMap<>();
        if (request.webhookSecret() != null && !request.webhookSecret().isBlank()) {
            config.put("webhookSecretConfigured", true);
        }
        if (request.botToken() != null && !request.botToken().isBlank()) {
            config.put("botTokenConfigured", true);
        }
        if (request.miniAppUrl() != null && !request.miniAppUrl().isBlank()) {
            config.put("miniAppUrl", request.miniAppUrl());
        }
        if (request.miniAppStartParam() != null && !request.miniAppStartParam().isBlank()) {
            config.put("miniAppStartParam", request.miniAppStartParam());
        }
        return config;
    }

    private String fingerprint(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed).substring(0, 16);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }
}
