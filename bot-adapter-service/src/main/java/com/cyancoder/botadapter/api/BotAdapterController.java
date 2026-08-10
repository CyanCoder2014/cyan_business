package com.cyancoder.botadapter.api;

import com.cyancoder.botadapter.domain.BotChannelIntegration;
import com.cyancoder.botadapter.domain.BotMiniAppBuild;
import com.cyancoder.botadapter.domain.BotOutboundMessage;
import com.cyancoder.botadapter.domain.BotProcessBinding;
import com.cyancoder.botadapter.domain.BotProcessDispatch;
import com.cyancoder.botadapter.service.BotAdapterService;
import com.cyancoder.botadapter.service.TenantMembershipClient;
import java.security.Principal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class BotAdapterController {
    private final BotAdapterService botAdapterService;
    private final TenantMembershipClient tenantMembershipClient;

    public BotAdapterController(BotAdapterService botAdapterService, TenantMembershipClient tenantMembershipClient) {
        this.botAdapterService = botAdapterService;
        this.tenantMembershipClient = tenantMembershipClient;
    }

    @PostMapping("/endpoint/bot-adapter/integrations")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public BotChannelIntegration upsertIntegration(@RequestBody BotIntegrationRequest request, Principal principal) {
        tenantMembershipClient.requireMembership(request.tenantKey(), principal.getName());
        return botAdapterService.upsertIntegration(request);
    }

    @GetMapping("/endpoint/bot-adapter/integrations")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public List<BotChannelIntegration> listIntegrations(
            @RequestParam(value = "tenantKey", required = false) String tenantKey,
            @RequestParam(value = "siteKey", required = false) String siteKey,
            Principal principal
    ) {
        tenantMembershipClient.requireMembership(tenantKey, principal.getName());
        return botAdapterService.listIntegrations(tenantKey, siteKey);
    }

    @PostMapping("/endpoint/bot-adapter/integrations/{channel}/{integrationKey}/register-webhook")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public WebhookRegistrationResult registerWebhook(@PathVariable String channel, @PathVariable String integrationKey,
                                                     @RequestHeader("X-Tenant-Key") String tenantKey,
                                                     @RequestHeader("X-Site-Key") String siteKey,
                                                     Principal principal) {
        tenantMembershipClient.requireMembership(tenantKey, principal.getName());
        return botAdapterService.registerWebhook(channel, integrationKey, tenantKey, siteKey);
    }

    @PostMapping("/endpoint/bot-adapter/messages")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')")
    public OutboundMessageResult sendMessage(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                             @RequestHeader("X-Tenant-Key") String tenantKey,
                                             Principal principal,
                                             @RequestBody OutboundMessageRequest request) {
        tenantMembershipClient.requireMembership(tenantKey, principal.getName());
        return botAdapterService.sendOutboundMessage(request, idempotencyKey);
    }

    @GetMapping("/endpoint/bot-adapter/messages")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')")
    public List<BotOutboundMessage> listMessages(
            @RequestParam(value = "tenantKey", required = false) String tenantKey,
            @RequestParam(value = "siteKey", required = false) String siteKey,
            @RequestParam(value = "integrationKey", required = false) String integrationKey,
            Principal principal
    ) {
        tenantMembershipClient.requireMembership(tenantKey, principal.getName());
        return botAdapterService.listOutboundMessages(tenantKey, siteKey, integrationKey);
    }

    @PostMapping("/endpoint/bot-adapter/messages/{messageId}/retry")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')")
    public RetryOutboundMessageResult retryMessage(@PathVariable String messageId,
                                                    @RequestHeader("X-Tenant-Key") String tenantKey,
                                                    Principal principal) {
        tenantMembershipClient.requireMembership(tenantKey, principal.getName());
        return botAdapterService.retryOutboundMessage(messageId);
    }

    @PostMapping("/endpoint/bot-adapter/mini-apps")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public BotMiniAppBuild upsertMiniAppBuild(@RequestBody BotMiniAppBuildRequest request,
                                              @RequestHeader("X-Tenant-Key") String tenantKey,
                                              Principal principal) {
        tenantMembershipClient.requireMembership(tenantKey, principal.getName());
        return botAdapterService.upsertMiniAppBuild(request);
    }

    @GetMapping("/endpoint/bot-adapter/mini-apps")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public List<BotMiniAppBuild> listMiniAppBuilds(
            @RequestParam(value = "tenantKey", required = false) String tenantKey,
            @RequestParam(value = "siteKey", required = false) String siteKey,
            Principal principal
    ) {
        tenantMembershipClient.requireMembership(tenantKey, principal.getName());
        return botAdapterService.listMiniAppBuilds(tenantKey, siteKey);
    }

    @PostMapping("/endpoint/bot-adapter/mini-apps/{channel}/{integrationKey}/{buildKey}/publish")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public BotMiniAppBuild publishMiniAppBuild(@PathVariable String channel,
                                               @PathVariable String integrationKey,
                                               @PathVariable String buildKey,
                                               @RequestHeader("X-Tenant-Key") String tenantKey,
                                               Principal principal) {
        tenantMembershipClient.requireMembership(tenantKey, principal.getName());
        return botAdapterService.publishMiniAppBuild(channel, integrationKey, buildKey);
    }

    @PostMapping("/endpoint/bot-adapter/integrations/{channel}/{integrationKey}/process-bindings")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public BotProcessBinding upsertProcessBinding(
            @PathVariable String channel,
            @PathVariable String integrationKey,
            @RequestHeader("X-Tenant-Key") String tenantKey,
            @RequestHeader("X-Site-Key") String siteKey,
            Principal principal,
            @RequestBody BotProcessBindingRequest request) {
        tenantMembershipClient.requireMembership(tenantKey, principal.getName());
        return botAdapterService.upsertProcessBinding(channel, integrationKey, tenantKey, siteKey, request);
    }

    @GetMapping("/endpoint/bot-adapter/integrations/{channel}/{integrationKey}/process-bindings")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public List<BotProcessBinding> listProcessBindings(
            @PathVariable String channel,
            @PathVariable String integrationKey,
            @RequestHeader("X-Tenant-Key") String tenantKey,
            @RequestHeader("X-Site-Key") String siteKey,
            Principal principal) {
        tenantMembershipClient.requireMembership(tenantKey, principal.getName());
        return botAdapterService.listProcessBindings(channel, integrationKey, tenantKey, siteKey);
    }

    @GetMapping("/endpoint/bot-adapter/integrations/{channel}/{integrationKey}/process-bindings/{bindingKey}/dispatches")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')")
    public List<BotProcessDispatch> listProcessDispatches(
            @PathVariable String channel,
            @PathVariable String integrationKey,
            @PathVariable String bindingKey,
            @RequestHeader("X-Tenant-Key") String tenantKey,
            @RequestHeader("X-Site-Key") String siteKey,
            Principal principal) {
        tenantMembershipClient.requireMembership(tenantKey, principal.getName());
        return botAdapterService.listProcessDispatches(channel, integrationKey, bindingKey, tenantKey, siteKey);
    }

    @PostMapping("/public/bot-adapter/{channel}/{integrationKey}/webhook")
    public WebhookResult webhook(@PathVariable String channel,
                                 @PathVariable String integrationKey,
                                 @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false) String telegramSecret,
                                 @RequestHeader(value = "X-Bot-Api-Secret-Token", required = false) String botSecret,
                                 @RequestBody Map<String, Object> payload) {
        return botAdapterService.handleWebhook(channel, integrationKey,
                telegramSecret == null || telegramSecret.isBlank() ? botSecret : telegramSecret, payload);
    }
}
