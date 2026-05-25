package com.cyancoder.botadapter.api;

import com.cyancoder.botadapter.domain.BotChannelIntegration;
import com.cyancoder.botadapter.domain.BotMiniAppBuild;
import com.cyancoder.botadapter.domain.BotOutboundMessage;
import com.cyancoder.botadapter.service.BotAdapterService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class BotAdapterController {
    private final BotAdapterService botAdapterService;

    public BotAdapterController(BotAdapterService botAdapterService) {
        this.botAdapterService = botAdapterService;
    }

    @PostMapping("/endpoint/bot-adapter/integrations")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public BotChannelIntegration upsertIntegration(@RequestBody BotIntegrationRequest request) {
        return botAdapterService.upsertIntegration(request);
    }

    @GetMapping("/endpoint/bot-adapter/integrations")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public List<BotChannelIntegration> listIntegrations(
            @RequestParam(value = "tenantKey", required = false) String tenantKey,
            @RequestParam(value = "siteKey", required = false) String siteKey
    ) {
        return botAdapterService.listIntegrations(tenantKey, siteKey);
    }

    @PostMapping("/endpoint/bot-adapter/integrations/{channel}/{integrationKey}/register-webhook")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public WebhookRegistrationResult registerWebhook(@PathVariable String channel, @PathVariable String integrationKey) {
        return botAdapterService.registerWebhook(channel, integrationKey);
    }

    @PostMapping("/endpoint/bot-adapter/messages")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')")
    public OutboundMessageResult sendMessage(@RequestBody OutboundMessageRequest request) {
        return botAdapterService.sendOutboundMessage(request);
    }

    @GetMapping("/endpoint/bot-adapter/messages")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')")
    public List<BotOutboundMessage> listMessages(
            @RequestParam(value = "tenantKey", required = false) String tenantKey,
            @RequestParam(value = "siteKey", required = false) String siteKey,
            @RequestParam(value = "integrationKey", required = false) String integrationKey
    ) {
        return botAdapterService.listOutboundMessages(tenantKey, siteKey, integrationKey);
    }

    @PostMapping("/endpoint/bot-adapter/messages/{messageId}/retry")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')")
    public RetryOutboundMessageResult retryMessage(@PathVariable String messageId) {
        return botAdapterService.retryOutboundMessage(messageId);
    }

    @PostMapping("/endpoint/bot-adapter/mini-apps")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public BotMiniAppBuild upsertMiniAppBuild(@RequestBody BotMiniAppBuildRequest request) {
        return botAdapterService.upsertMiniAppBuild(request);
    }

    @GetMapping("/endpoint/bot-adapter/mini-apps")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public List<BotMiniAppBuild> listMiniAppBuilds(
            @RequestParam(value = "tenantKey", required = false) String tenantKey,
            @RequestParam(value = "siteKey", required = false) String siteKey
    ) {
        return botAdapterService.listMiniAppBuilds(tenantKey, siteKey);
    }

    @PostMapping("/endpoint/bot-adapter/mini-apps/{channel}/{integrationKey}/{buildKey}/publish")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public BotMiniAppBuild publishMiniAppBuild(@PathVariable String channel,
                                               @PathVariable String integrationKey,
                                               @PathVariable String buildKey) {
        return botAdapterService.publishMiniAppBuild(channel, integrationKey, buildKey);
    }

    @PostMapping("/public/bot-adapter/{channel}/{integrationKey}/webhook")
    public WebhookResult webhook(@PathVariable String channel,
                                 @PathVariable String integrationKey,
                                 @RequestBody Map<String, Object> payload) {
        return botAdapterService.handleWebhook(channel, integrationKey, payload);
    }
}
