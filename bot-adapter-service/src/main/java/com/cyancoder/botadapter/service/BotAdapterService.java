package com.cyancoder.botadapter.service;

import com.cyancoder.botadapter.api.BotIntegrationRequest;
import com.cyancoder.botadapter.api.BotProcessBindingRequest;
import com.cyancoder.botadapter.api.BotMiniAppBuildRequest;
import com.cyancoder.botadapter.api.OutboundMessageRequest;
import com.cyancoder.botadapter.api.OutboundMessageResult;
import com.cyancoder.botadapter.api.RetryOutboundMessageResult;
import com.cyancoder.botadapter.api.WebhookResult;
import com.cyancoder.botadapter.api.WebhookRegistrationResult;
import com.cyancoder.botadapter.domain.BotChannel;
import com.cyancoder.botadapter.domain.BotChannelIntegration;
import com.cyancoder.botadapter.domain.BotChatSessionMapping;
import com.cyancoder.botadapter.domain.BotInboundMessage;
import com.cyancoder.botadapter.domain.BotMiniAppBuild;
import com.cyancoder.botadapter.domain.BotOutboundMessage;
import com.cyancoder.botadapter.domain.BotProcessBinding;
import com.cyancoder.botadapter.domain.BotProcessDispatch;
import com.cyancoder.botadapter.domain.BotProcessTargetType;
import com.cyancoder.botadapter.domain.BotProcessTriggerType;
import com.cyancoder.botadapter.repo.BotChannelIntegrationRepository;
import com.cyancoder.botadapter.repo.BotChatSessionMappingRepository;
import com.cyancoder.botadapter.repo.BotInboundMessageRepository;
import com.cyancoder.botadapter.repo.BotMiniAppBuildRepository;
import com.cyancoder.botadapter.repo.BotOutboundMessageRepository;
import com.cyancoder.botadapter.repo.BotProcessBindingRepository;
import com.cyancoder.botadapter.repo.BotProcessDispatchRepository;
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
    private final BotMiniAppBuildRepository miniAppBuildRepository;
    private final BotWebhookParser webhookParser;
    private final AiConversationClient aiConversationClient;
    private final BotProviderClient botProviderClient;
    private final BotProcessBindingRepository processBindingRepository;
    private final BotProcessDispatchRepository processDispatchRepository;
    private final BotProcessClient botProcessClient;

    public BotAdapterService(BotChannelIntegrationRepository integrationRepository,
                             BotChatSessionMappingRepository mappingRepository,
                             BotInboundMessageRepository inboundMessageRepository,
                             BotOutboundMessageRepository outboundMessageRepository,
                             BotMiniAppBuildRepository miniAppBuildRepository,
                             BotWebhookParser webhookParser,
                             AiConversationClient aiConversationClient,
                             BotProviderClient botProviderClient,
                             BotProcessBindingRepository processBindingRepository,
                             BotProcessDispatchRepository processDispatchRepository,
                             BotProcessClient botProcessClient) {
        this.integrationRepository = integrationRepository;
        this.mappingRepository = mappingRepository;
        this.inboundMessageRepository = inboundMessageRepository;
        this.outboundMessageRepository = outboundMessageRepository;
        this.miniAppBuildRepository = miniAppBuildRepository;
        this.webhookParser = webhookParser;
        this.aiConversationClient = aiConversationClient;
        this.botProviderClient = botProviderClient;
        this.processBindingRepository = processBindingRepository;
        this.processDispatchRepository = processDispatchRepository;
        this.botProcessClient = botProcessClient;
    }

    public BotChannelIntegration upsertIntegration(BotIntegrationRequest request) {
        if (request.botToken() != null && !request.botToken().isBlank()) {
            throw new IllegalArgumentException("botToken is not accepted; configure tokenSecretRef");
        }
        if (request.webhookSecret() != null && !request.webhookSecret().isBlank()) {
            throw new IllegalArgumentException("webhookSecret is not accepted; configure webhookSecretRef");
        }
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
        integration.setTokenSecretRef(required(request.tokenSecretRef(), "tokenSecretRef"));
        integration.setWebhookSecretRef(request.webhookSecretRef());
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
        required(tenantKey, "tenantKey");
        required(siteKey, "siteKey");
        if (tenantKey != null && !tenantKey.isBlank() && siteKey != null && !siteKey.isBlank()) {
            return integrationRepository.findByTenantKeyAndSiteKeyOrderByUpdatedAtDesc(tenantKey, siteKey);
        }
        return List.of();
    }

    public WebhookResult handleWebhook(String channelValue, String integrationKey, String suppliedWebhookSecret,
                                       Map<String, Object> payload) {
        BotChannel channel = parseChannel(channelValue);
        BotChannelIntegration integration = integrationRepository
                .findByChannelAndIntegrationKeyAndActiveTrue(channel, integrationKey)
                .orElseThrow(() -> new IllegalArgumentException("Active bot integration not found"));
        botProviderClient.verifyWebhookSecret(integration, suppliedWebhookSecret);
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

        BotInboundMessage inbound = new BotInboundMessage();
        inbound.setChannel(channel);
        inbound.setIntegrationKey(integrationKey);
        inbound.setExternalMessageId(parsed.messageId());
        inbound.setExternalChatId(parsed.chatId());
        inbound.setText(parsed.text());
        inbound.setRawPayload(payload);
        inbound.setStatus("RECEIVED");
        inbound.setReceivedAt(Instant.now());
        inbound = inboundMessageRepository.save(inbound);

        dispatchProcessBindings(integration, inbound);

        try {
            BotChatSessionMapping mapping = mappingRepository
                    .findByChannelAndIntegrationKeyAndExternalChatId(channel, integrationKey, parsed.chatId())
                    .orElseGet(() -> createMapping(integration, parsed.chatId()));
            aiConversationClient.appendUserMessage(mapping.getSessionId(), parsed.text(), Map.of(
                    "channel", channel.name().toLowerCase(Locale.ROOT),
                    "externalChatId", parsed.chatId(),
                    "integrationKey", integrationKey
            ));
            inbound.setSessionId(mapping.getSessionId());
            inbound.setStatus("PROCESSED");
        } catch (RuntimeException ignored) {
            inbound.setStatus("PROCESS_DISPATCHED_AI_UNAVAILABLE");
        }
        inboundMessageRepository.save(inbound);
        return new WebhookResult("ACCEPTED", inbound.getSessionId(), parsed.messageId(), parsed.chatId());
    }

    public WebhookRegistrationResult registerWebhook(String channelValue, String integrationKey) {
        BotChannelIntegration integration = findActiveIntegration(channelValue, integrationKey);
        botProviderClient.registerWebhook(integration);
        String webhookUrl = botProviderClient.webhookUrl(integration);
        return new WebhookRegistrationResult(
                "REGISTERED",
                integration.getChannel().name(),
                integration.getIntegrationKey(),
                webhookUrl
        );
    }

    public WebhookRegistrationResult registerWebhook(String channelValue, String integrationKey, String tenantKey, String siteKey) {
        BotChannelIntegration integration = scopedIntegration(channelValue, integrationKey, tenantKey, siteKey);
        botProviderClient.registerWebhook(integration);
        return new WebhookRegistrationResult("REGISTERED", integration.getChannel().name(), integration.getIntegrationKey(), botProviderClient.webhookUrl(integration));
    }

    public OutboundMessageResult sendOutboundMessage(OutboundMessageRequest request) {
        return sendOutboundMessage(request, null);
    }

    public OutboundMessageResult sendOutboundMessage(OutboundMessageRequest request, String idempotencyKey) {
        BotChannelIntegration integration = findActiveIntegration(request.channel(), request.integrationKey());
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing = outboundMessageRepository.findByIntegrationKeyAndIdempotencyKey(integration.getIntegrationKey(), idempotencyKey);
            if (existing.isPresent()) {
                BotOutboundMessage value = existing.get();
                return new OutboundMessageResult(value.getStatus(), value.getChannel().name(), value.getExternalChatId(), value.getText(), value.getId(), value.getAttemptCount());
            }
        }
        BotOutboundMessage message = new BotOutboundMessage();
        message.setChannel(integration.getChannel());
        message.setIntegrationKey(integration.getIntegrationKey());
        message.setTenantKey(integration.getTenantKey());
        message.setSiteKey(integration.getSiteKey());
        message.setClientKey(integration.getClientKey());
        message.setExternalChatId(required(request.externalChatId(), "externalChatId"));
        message.setText(required(request.text(), "text"));
        message.setIdempotencyKey(idempotencyKey);
        message.setStatus("PENDING");
        message.setCreatedAt(Instant.now());
        message.setUpdatedAt(Instant.now());
        message = outboundMessageRepository.save(message);
        return dispatchOutboundMessage(integration, message);
    }

    public List<BotOutboundMessage> listOutboundMessages(String tenantKey, String siteKey, String integrationKey) {
        required(tenantKey, "tenantKey");
        required(siteKey, "siteKey");
        if (tenantKey != null && !tenantKey.isBlank() && siteKey != null && !siteKey.isBlank()) {
            return outboundMessageRepository.findByTenantKeyAndSiteKeyOrderByUpdatedAtDesc(tenantKey, siteKey);
        }
        return List.of();
    }

    public RetryOutboundMessageResult retryOutboundMessage(String messageId) {
        BotOutboundMessage message = outboundMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Outbound message not found"));
        BotChannelIntegration integration = findActiveIntegration(message.getChannel().name(), message.getIntegrationKey());
        OutboundMessageResult result = dispatchOutboundMessage(integration, message);
        return new RetryOutboundMessageResult(result.status(), result.deliveryId(), result.attemptCount());
    }

    public BotMiniAppBuild upsertMiniAppBuild(BotMiniAppBuildRequest request) {
        BotChannelIntegration integration = findActiveIntegration(request.channel(), request.integrationKey());
        String buildKey = required(request.buildKey(), "buildKey");
        BotMiniAppBuild build = miniAppBuildRepository
                .findByChannelAndIntegrationKeyAndBuildKey(integration.getChannel(), integration.getIntegrationKey(), buildKey)
                .orElseGet(BotMiniAppBuild::new);
        build.setChannel(integration.getChannel());
        build.setIntegrationKey(integration.getIntegrationKey());
        build.setBuildKey(buildKey);
        build.setTenantKey(integration.getTenantKey());
        build.setSiteKey(integration.getSiteKey());
        build.setTitle(firstNonBlank(request.title(), buildKey));
        build.setLaunchUrl(firstNonBlank(request.launchUrl(), integration.getMiniAppUrl()));
        build.setManifest(request.manifest() == null ? Map.of() : request.manifest());
        build.setStatus("DRAFT");
        if (build.getCreatedAt() == null) {
            build.setCreatedAt(Instant.now());
        }
        build.setUpdatedAt(Instant.now());
        return miniAppBuildRepository.save(build);
    }

    public List<BotMiniAppBuild> listMiniAppBuilds(String tenantKey, String siteKey) {
        required(tenantKey, "tenantKey");
        required(siteKey, "siteKey");
        if (tenantKey != null && !tenantKey.isBlank() && siteKey != null && !siteKey.isBlank()) {
            return miniAppBuildRepository.findByTenantKeyAndSiteKeyOrderByUpdatedAtDesc(tenantKey, siteKey);
        }
        return List.of();
    }

    public BotMiniAppBuild publishMiniAppBuild(String channelValue, String integrationKey, String buildKey) {
        BotChannel channel = parseChannel(channelValue);
        BotMiniAppBuild build = miniAppBuildRepository.findByChannelAndIntegrationKeyAndBuildKey(channel, integrationKey, buildKey)
                .orElseThrow(() -> new IllegalArgumentException("Mini app build not found"));
        build.setStatus("PUBLISHED");
        if (build.getLaunchUrl() == null || build.getLaunchUrl().isBlank()) {
            throw new IllegalStateException("NOT_CONFIGURED: mini app launch URL is required before publishing");
        }
        build.setPublishedUrl(build.getLaunchUrl());
        build.setUpdatedAt(Instant.now());
        return miniAppBuildRepository.save(build);
    }

    public BotProcessBinding upsertProcessBinding(String channelValue, String integrationKey,
                                                  String tenantKey, String siteKey,
                                                  BotProcessBindingRequest request) {
        BotChannelIntegration integration = scopedIntegration(channelValue, integrationKey, tenantKey, siteKey);
        String bindingKey = required(request.bindingKey(), "bindingKey");
        BotProcessBinding binding = processBindingRepository
                .findByChannelAndIntegrationKeyAndBindingKey(integration.getChannel(), integrationKey, bindingKey)
                .orElseGet(BotProcessBinding::new);
        binding.setChannel(integration.getChannel());
        binding.setIntegrationKey(integrationKey);
        binding.setBindingKey(bindingKey);
        binding.setTenantKey(integration.getTenantKey());
        binding.setSiteKey(integration.getSiteKey());
        binding.setTriggerType(parseEnum(request.triggerType(), BotProcessTriggerType.class, "triggerType"));
        binding.setCommandPrefix(request.commandPrefix());
        if (binding.getTriggerType() == BotProcessTriggerType.COMMAND) {
            required(request.commandPrefix(), "commandPrefix");
        }
        binding.setTargetType(parseEnum(request.targetType(), BotProcessTargetType.class, "targetType"));
        binding.setTargetKey(required(request.targetKey(), "targetKey"));
        binding.setInputTemplate(request.inputTemplate());
        binding.setEnabled(request.enabled() == null || request.enabled());
        if (binding.getCreatedAt() == null) binding.setCreatedAt(Instant.now());
        binding.setUpdatedAt(Instant.now());
        botProcessClient.validateTarget(binding);
        return processBindingRepository.save(binding);
    }

    public List<BotProcessBinding> listProcessBindings(String channelValue, String integrationKey,
                                                       String tenantKey, String siteKey) {
        BotChannelIntegration integration = scopedIntegration(channelValue, integrationKey, tenantKey, siteKey);
        return processBindingRepository.findByTenantKeyAndSiteKeyAndIntegrationKeyOrderByUpdatedAtDesc(
                integration.getTenantKey(), integration.getSiteKey(), integrationKey);
    }

    public List<BotProcessDispatch> listProcessDispatches(String channelValue, String integrationKey,
                                                          String bindingKey, String tenantKey, String siteKey) {
        scopedIntegration(channelValue, integrationKey, tenantKey, siteKey);
        return processDispatchRepository.findByTenantKeyAndSiteKeyAndBindingKeyOrderByCreatedAtDesc(
                required(tenantKey, "X-Tenant-Key"), required(siteKey, "X-Site-Key"), required(bindingKey, "bindingKey"));
    }

    private void dispatchProcessBindings(BotChannelIntegration integration, BotInboundMessage inbound) {
        for (BotProcessBinding binding : processBindingRepository
                .findByChannelAndIntegrationKeyAndEnabledTrueOrderByUpdatedAtDesc(integration.getChannel(), integration.getIntegrationKey())) {
            if (!matches(binding, inbound.getText())) continue;
            BotProcessDispatch dispatch = processDispatchRepository
                    .findByBindingIdAndInboundMessageId(binding.getId(), inbound.getId())
                    .orElseGet(BotProcessDispatch::new);
            if (dispatch.getId() != null) continue;
            dispatch.setBindingId(binding.getId());
            dispatch.setBindingKey(binding.getBindingKey());
            dispatch.setInboundMessageId(inbound.getId());
            dispatch.setTenantKey(binding.getTenantKey());
            dispatch.setSiteKey(binding.getSiteKey());
            dispatch.setTargetType(binding.getTargetType());
            dispatch.setTargetKey(binding.getTargetKey());
            dispatch.setStatus("STARTING");
            dispatch.setCreatedAt(Instant.now());
            dispatch.setUpdatedAt(Instant.now());
            dispatch = processDispatchRepository.save(dispatch);
            try {
                dispatch.setTargetReference(botProcessClient.dispatch(binding, inbound.getId(), inbound.getExternalMessageId(),
                        inbound.getExternalChatId(), inbound.getText(), inbound.getChannel().name(), inbound.getIntegrationKey()));
                dispatch.setStatus("STARTED");
            } catch (RuntimeException ex) {
                dispatch.setStatus("FAILED");
                dispatch.setErrorCode("TARGET_UNAVAILABLE");
                dispatch.setErrorMessage(safeMessage(ex));
            }
            dispatch.setUpdatedAt(Instant.now());
            processDispatchRepository.save(dispatch);
        }
    }

    private boolean matches(BotProcessBinding binding, String text) {
        return binding.getTriggerType() == BotProcessTriggerType.EVERY_MESSAGE
                || (text != null && text.trim().startsWith(binding.getCommandPrefix().trim()));
    }

    private BotChannelIntegration scopedIntegration(String channelValue, String integrationKey, String tenantKey, String siteKey) {
        BotChannelIntegration integration = findActiveIntegration(channelValue, integrationKey);
        if (!integration.getTenantKey().equals(required(tenantKey, "X-Tenant-Key"))
                || !integration.getSiteKey().equals(required(siteKey, "X-Site-Key"))) {
            throw new IllegalArgumentException("Bot integration is outside the requested tenant/site scope");
        }
        return integration;
    }

    private <T extends Enum<T>> T parseEnum(String value, Class<T> type, String field) {
        try {
            return Enum.valueOf(type, required(value, field).trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(field + " is invalid");
        }
    }

    private String safeMessage(RuntimeException ex) {
        String message = ex.getMessage();
        return message == null ? ex.getClass().getSimpleName() : message.substring(0, Math.min(message.length(), 500));
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

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
