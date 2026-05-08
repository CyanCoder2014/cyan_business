package com.cyancoder.aiorchestrator.service.impl;

import com.cyancoder.aiorchestrator.api.dto.ProvisionDraftRequest;
import com.cyancoder.aiorchestrator.api.dto.ProvisioningResultDto;
import com.cyancoder.aiorchestrator.api.dto.ProvisioningRunDto;
import com.cyancoder.aiorchestrator.domain.ClientAppDraft;
import com.cyancoder.aiorchestrator.domain.DraftStatus;
import com.cyancoder.aiorchestrator.domain.ProvisioningRun;
import com.cyancoder.aiorchestrator.domain.ProvisioningRunStatus;
import com.cyancoder.aiorchestrator.domain.ProvisioningStepResult;
import com.cyancoder.aiorchestrator.repo.ClientAppDraftRepository;
import com.cyancoder.aiorchestrator.repo.ProvisioningRunRepository;
import com.cyancoder.aiorchestrator.service.ProvisioningRunService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class MongoProvisioningRunService implements ProvisioningRunService {
    private final ClientAppDraftRepository draftRepository;
    private final ProvisioningRunRepository runRepository;
    private final PlatformProvisioningService platformProvisioningService;

    public MongoProvisioningRunService(ClientAppDraftRepository draftRepository,
                                       ProvisioningRunRepository runRepository,
                                       PlatformProvisioningService platformProvisioningService) {
        this.draftRepository = draftRepository;
        this.runRepository = runRepository;
        this.platformProvisioningService = platformProvisioningService;
    }

    @Override
    public ProvisioningRunDto provisionDraft(String draftId, ProvisionDraftRequest request) {
        ClientAppDraft draft = draftRepository.findByDraftId(draftId).orElseThrow();
        String mode = request == null || request.mode() == null || request.mode().isBlank() ? "APPLY" : request.mode().trim().toUpperCase();

        ProvisioningRun run = new ProvisioningRun();
        run.setRunId("run-" + UUID.randomUUID());
        run.setDraftId(draft.getDraftId());
        run.setTenantKey(draft.getTenantKey());
        run.setSiteKey(draft.getSiteKey());
        run.setTriggerType(request == null || request.triggerType() == null || request.triggerType().isBlank() ? "API" : request.triggerType());
        run.setTriggeredBy(request == null || request.triggeredBy() == null || request.triggeredBy().isBlank() ? "endpoint-user" : request.triggeredBy());
        run.setStatus("PLAN".equals(mode) ? ProvisioningRunStatus.PLANNED : ProvisioningRunStatus.RUNNING);
        run.setStartedAt(Instant.now());
        run = runRepository.save(run);

        if (!draft.getPendingQuestions().isEmpty()) {
            ProvisioningStepResult step = new ProvisioningStepResult();
            step.setStepKey("guard:ai-orchestrator:follow-up-questions");
            step.setServiceKey("ai-orchestrator-service");
            step.setEndpointPath("/endpoint/ai-orchestrator/drafts/" + draftId + "/provision");
            step.setStatus("BLOCKED");
            step.setIdempotencyKey(request == null ? null : request.idempotencyKey());
            step.setSummary(String.join(" | ", draft.getPendingQuestions()));
            step.setResponse(java.util.Map.of(
                    "pendingQuestionKeys", draft.getPendingQuestionKeys(),
                    "pendingQuestions", draft.getPendingQuestions()
            ));
            run.getStepResults().add(step);
            run.setStatus(ProvisioningRunStatus.BLOCKED);
            run.setFinishedAt(Instant.now());
            run = runRepository.save(run);
            return toDto(run, null);
        }

        if ("PLAN".equals(mode)) {
            run.setFinishedAt(Instant.now());
            run = runRepository.save(run);
            return toDto(run, null);
        }

        draft.setStatus(DraftStatus.PROVISIONING);
        draft.setUpdatedAt(Instant.now());
        draftRepository.save(draft);

        try {
            ProvisioningResultDto result = platformProvisioningService.provision(draft.getDraftId(), draft.getResolvedDsl(), run, request == null ? null : request.idempotencyKey());
            draft.setStatus(DraftStatus.PROVISIONED);
            draft.setUpdatedAt(Instant.now());
            draftRepository.save(draft);
            return toDto(runRepository.findById(run.getId()).orElseThrow(), result);
        } catch (RuntimeException ex) {
            draft.setStatus(DraftStatus.FAILED);
            draft.setUpdatedAt(Instant.now());
            draftRepository.save(draft);
            return toDto(runRepository.findById(run.getId()).orElseThrow(), null);
        }
    }

    @Override
    public List<ProvisioningRunDto> listRuns(String draftId) {
        return runRepository.findByDraftIdOrderByStartedAtDesc(draftId).stream()
                .map(run -> toDto(run, null))
                .toList();
    }

    @Override
    public ProvisioningRunDto getRun(String runId) {
        return toDto(runRepository.findByRunId(runId).orElseThrow(), null);
    }

    private ProvisioningRunDto toDto(ProvisioningRun run, ProvisioningResultDto result) {
        return new ProvisioningRunDto(
                run.getRunId(),
                run.getDraftId(),
                run.getTenantKey(),
                run.getSiteKey(),
                run.getStatus(),
                run.getTriggerType(),
                run.getTriggeredBy(),
                run.getStartedAt(),
                run.getFinishedAt(),
                run.getStepResults(),
                result
        );
    }
}
