package com.cyancoder.automationorchestrator.repo;

import com.cyancoder.automationorchestrator.domain.AutomationExecution;

import java.time.Instant;
import java.util.Optional;

public interface AutomationExecutionClaimRepository {
    Optional<AutomationExecution> claimNextRecoverable(
            String workerId,
            Instant now,
            Instant staleBefore,
            Instant leaseUntil
    );

    long renewLeases(String workerId, Instant heartbeatAt, Instant leaseUntil);
}
