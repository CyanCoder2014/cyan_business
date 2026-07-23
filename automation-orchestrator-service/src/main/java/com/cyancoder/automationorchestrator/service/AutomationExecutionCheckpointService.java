package com.cyancoder.automationorchestrator.service;

import com.cyancoder.automationorchestrator.config.AutomationWorkerProperties;
import com.cyancoder.automationorchestrator.domain.AutomationExecution;
import com.cyancoder.automationorchestrator.repo.AutomationExecutionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AutomationExecutionCheckpointService {
    private final AutomationExecutionRepository repository;
    private final AutomationWorkerProperties workerProperties;

    public AutomationExecutionCheckpointService(
            AutomationExecutionRepository repository,
            AutomationWorkerProperties workerProperties
    ) {
        this.repository = repository;
        this.workerProperties = workerProperties;
    }

    public void checkpoint(AutomationExecution execution) {
        if (execution == null || execution.getId() == null) return;
        Instant now = Instant.now();
        execution.setCheckpointSequence(execution.getCheckpointSequence() + 1);
        execution.setHeartbeatAt(now);
        execution.setLeaseUntil(now.plus(workerProperties.getLeaseDuration()));
        execution.setUpdatedAt(now);
        repository.save(execution);
    }
}
