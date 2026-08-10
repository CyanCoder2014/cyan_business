package com.cyancoder.aiorchestrator.api;

import com.cyancoder.aiorchestrator.api.dto.CreateDraftRequest;
import com.cyancoder.aiorchestrator.api.dto.UpdateDraftRequest;
import com.cyancoder.aiorchestrator.api.dto.AttachProjectAssetRequest;
import com.cyancoder.aiorchestrator.domain.ClientAppDraft;
import com.cyancoder.aiorchestrator.domain.ProjectAssetReference;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestHeader;
import com.cyancoder.aiorchestrator.service.AppDraftService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.time.Instant;

@RestController
@RequestMapping("/endpoint/ai-orchestrator/drafts")
public class EndpointDraftController {
    private final AppDraftService appDraftService;

    public EndpointDraftController(AppDraftService appDraftService) {
        this.appDraftService = appDraftService;
    }

    @PostMapping
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public ClientAppDraft create(@RequestBody CreateDraftRequest request) {
        return appDraftService.createDraft(request, "endpoint-user");
    }

    @GetMapping
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public List<ClientAppDraft> list(
            @RequestParam(value = "tenantKey", required = false) String tenantKey,
            @RequestParam(value = "siteKey", required = false) String siteKey,
            @RequestParam(value = "clientKey", required = false) String clientKey
    ) {
        return appDraftService.listDrafts(tenantKey, siteKey, clientKey);
    }

    @GetMapping("/{draftId}")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public ClientAppDraft get(@PathVariable("draftId") String draftId) {
        return appDraftService.getDraft(draftId);
    }

    @PatchMapping("/{draftId}")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public ClientAppDraft update(@PathVariable("draftId") String draftId, @RequestBody UpdateDraftRequest request) {
        return appDraftService.updateDraft(draftId, request, "endpoint-user");
    }

    @PostMapping("/{draftId}/attachments")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public ProjectAssetReference attach(@PathVariable String draftId, @RequestHeader("X-Tenant-Key") String tenantKey,
                                        @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
                                        @Valid @RequestBody AttachProjectAssetRequest request, Authentication authentication) {
        return appDraftService.attachAsset(draftId, tenantKey, siteKey,
                new ProjectAssetReference(request.assetKey(), request.fileName(), request.mimeType(), request.sizeBytes(), authentication.getName(), Instant.now()));
    }
}
