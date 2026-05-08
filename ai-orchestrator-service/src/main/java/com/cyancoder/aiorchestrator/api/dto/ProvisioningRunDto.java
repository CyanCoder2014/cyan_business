package com.cyancoder.aiorchestrator.api.dto;

import com.cyancoder.aiorchestrator.domain.ProvisioningRunStatus;
import com.cyancoder.aiorchestrator.domain.ProvisioningStepResult;

import java.time.Instant;
import java.util.List;

public record ProvisioningRunDto(
        String runId,
        String draftId,
        String tenantKey,
        String siteKey,
        ProvisioningRunStatus status,
        String triggerType,
        String triggeredBy,
        Instant startedAt,
        Instant finishedAt,
        List<ProvisioningStepResult> stepResults,
        ProvisioningResultDto result
) {
}
