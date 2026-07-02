package com.cyancoder.aiorchestrator.api;

import com.cyancoder.aiorchestrator.api.dto.CreateDraftRequest;
import com.cyancoder.aiorchestrator.api.dto.UpdateDraftRequest;
import com.cyancoder.aiorchestrator.domain.ClientAppDraft;
import com.cyancoder.aiorchestrator.service.AppDraftService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/ai-orchestrator/drafts")
public class InternalDraftController {
    private final AppDraftService appDraftService;

    public InternalDraftController(AppDraftService appDraftService) {
        this.appDraftService = appDraftService;
    }

    @PostMapping
    public ClientAppDraft create(@RequestBody CreateDraftRequest request) {
        return appDraftService.createDraft(request, "internal-service");
    }

    @GetMapping
    public List<ClientAppDraft> list(
            @RequestParam(value = "tenantKey", required = false) String tenantKey,
            @RequestParam(value = "siteKey", required = false) String siteKey,
            @RequestParam(value = "clientKey", required = false) String clientKey
    ) {
        return appDraftService.listDrafts(tenantKey, siteKey, clientKey);
    }

    @GetMapping("/{draftId}")
    public ClientAppDraft get(@PathVariable("draftId") String draftId) {
        return appDraftService.getDraft(draftId);
    }

    @PatchMapping("/{draftId}")
    public ClientAppDraft update(@PathVariable("draftId") String draftId, @RequestBody UpdateDraftRequest request) {
        return appDraftService.updateDraft(draftId, request, "internal-service");
    }
}
