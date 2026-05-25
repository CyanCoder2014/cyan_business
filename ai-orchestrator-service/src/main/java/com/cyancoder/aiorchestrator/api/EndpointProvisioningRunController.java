package com.cyancoder.aiorchestrator.api;

import com.cyancoder.aiorchestrator.api.dto.ProvisionDraftRequest;
import com.cyancoder.aiorchestrator.api.dto.ProvisioningRunDto;
import com.cyancoder.aiorchestrator.service.ProvisioningRunService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/endpoint/ai-orchestrator")
public class EndpointProvisioningRunController {
    private final ProvisioningRunService provisioningRunService;

    public EndpointProvisioningRunController(ProvisioningRunService provisioningRunService) {
        this.provisioningRunService = provisioningRunService;
    }

    @PostMapping("/drafts/{draftId}/provision")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public ProvisioningRunDto provisionDraft(@PathVariable String draftId,
                                             @RequestBody(required = false) ProvisionDraftRequest request) {
        return provisioningRunService.provisionDraft(draftId, request);
    }

    @GetMapping("/drafts/{draftId}/runs")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public List<ProvisioningRunDto> listRuns(@PathVariable String draftId) {
        return provisioningRunService.listRuns(draftId);
    }

    @GetMapping("/runs/{runId}")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('builder:use')")
    public ProvisioningRunDto getRun(@PathVariable String runId) {
        return provisioningRunService.getRun(runId);
    }
}
