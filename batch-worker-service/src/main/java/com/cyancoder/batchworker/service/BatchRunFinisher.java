package com.cyancoder.batchworker.service;

import com.cyancoder.batchworker.domain.BatchRun;
import com.cyancoder.batchworker.domain.BatchRunStatus;
import com.cyancoder.batchworker.repository.BatchRunRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BatchRunFinisher {
    private final BatchRunRepository repository;

    public BatchRunFinisher(BatchRunRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void finish(UUID runId, String workerId, JobExecution execution, Throwable failure) {
        BatchRun run = repository.findById(runId).orElseThrow();
        if (!workerId.equals(run.getWorkerId())) {
            return;
        }
        run.setBatchExecutionId(execution == null ? run.getBatchExecutionId() : execution.getId());
        if (execution != null) {
            run.setReadCount(execution.getStepExecutions().stream().mapToLong(step -> step.getReadCount()).sum());
            run.setWriteCount(execution.getStepExecutions().stream().mapToLong(step -> step.getWriteCount()).sum());
            run.setSkipCount(execution.getStepExecutions().stream().mapToLong(step -> step.getSkipCount()).sum());
        }
        boolean completed = failure == null && execution != null
                && execution.getStatus() == org.springframework.batch.core.BatchStatus.COMPLETED;
        run.setStatus(completed ? BatchRunStatus.COMPLETED : BatchRunStatus.FAILED);
        run.setErrorMessage(completed ? null : truncate(failureMessage(execution, failure)));
        run.setCompletedAt(Instant.now());
        run.setWorkerId(null);
        run.setLeaseUntil(null);
        run.setHeartbeatAt(null);
        repository.save(run);
    }

    private String failureMessage(JobExecution execution, Throwable failure) {
        if (failure != null) {
            return failure.getClass().getSimpleName() + ": " + failure.getMessage();
        }
        if (execution != null && !execution.getAllFailureExceptions().isEmpty()) {
            Throwable cause = execution.getAllFailureExceptions().getFirst();
            return cause.getClass().getSimpleName() + ": " + cause.getMessage();
        }
        return execution == null ? "Batch launch failed" : execution.getExitStatus().toString();
    }

    private String truncate(String value) {
        if (value == null) return "Unknown batch failure";
        return value.substring(0, Math.min(value.length(), 4000));
    }
}
