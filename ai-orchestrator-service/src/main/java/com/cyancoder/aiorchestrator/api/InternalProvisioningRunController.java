package com.cyancoder.aiorchestrator.api;

import com.cyancoder.aiorchestrator.api.dto.ProvisionDraftRequest;
import com.cyancoder.aiorchestrator.api.dto.ProvisioningRunDto;
import com.cyancoder.aiorchestrator.service.ProvisioningRunService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/ai-orchestrator")
public class InternalProvisioningRunController {
    private final ProvisioningRunService provisioningRunService;

    public InternalProvisioningRunController(ProvisioningRunService provisioningRunService) {
        this.provisioningRunService = provisioningRunService;
    }

    @PostMapping("/drafts/{draftId}/provision")
    public ProvisioningRunDto provisionDraft(@PathVariable String draftId,
                                             @RequestBody(required = false) ProvisionDraftRequest request) {
        return provisioningRunService.provisionDraft(draftId, request);
    }

    @GetMapping("/drafts/{draftId}/runs")
    public List<ProvisioningRunDto> listRuns(@PathVariable String draftId) {
        return provisioningRunService.listRuns(draftId);
    }

    @GetMapping("/runs/{runId}")
    public ProvisioningRunDto getRun(@PathVariable String runId) {
        return provisioningRunService.getRun(runId);
    }
}
