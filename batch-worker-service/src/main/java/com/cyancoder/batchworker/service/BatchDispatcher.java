package com.cyancoder.batchworker.service;

import com.cyancoder.batchworker.api.BatchDefinitionSpec;
import com.cyancoder.batchworker.domain.BatchDefinition;
import com.cyancoder.batchworker.domain.BatchRun;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BatchDispatcher {
    private final BatchRunClaimService claims;
    private final BatchDefinitionService definitions;
    private final BatchJobFactory jobs;
    private final JobOperator jobOperator;
    private final JobRepository jobRepository;
    private final BatchRunFinisher finisher;
    private final String workerId = host() + ":" + UUID.randomUUID();
    private final AtomicReference<UUID> activeRun = new AtomicReference<>();

    public BatchDispatcher(BatchRunClaimService claims, BatchDefinitionService definitions,
            BatchJobFactory jobs, JobOperator jobOperator, JobRepository jobRepository,
            BatchRunFinisher finisher) {
        this.claims = claims;
        this.definitions = definitions;
        this.jobs = jobs;
        this.jobOperator = jobOperator;
        this.jobRepository = jobRepository;
        this.finisher = finisher;
    }

    @Scheduled(fixedDelayString = "${batch.worker.recovery-poll-ms:5000}")
    public void dispatch() {
        if (activeRun.get() != null) {
            return;
        }
        claims.claim(workerId).ifPresent(claim -> execute(claim.run(), claim.recovery()));
    }

    @Scheduled(fixedDelayString = "30000")
    public void heartbeat() {
        UUID runId = activeRun.get();
        if (runId != null) {
            claims.renew(runId, workerId);
        }
    }

    private void execute(BatchRun run, boolean recovery) {
        if (!activeRun.compareAndSet(null, run.getId())) {
            return;
        }
        JobExecution execution = null;
        Throwable failure = null;
        try {
            if (recovery) {
                execution = recoveredCompletion(run);
                if (execution == null) {
                    closeStaleExecution(run);
                }
            }
            if (execution == null) {
                BatchDefinition definition = definitions.get(
                        run.getTenantKey(), run.getSiteKey(), run.getDefinitionKey());
                BatchDefinitionSpec spec = definitions.spec(definition);
                Job job = jobs.create(run, spec);
                execution = jobOperator.start(job, parameters(run));
            }
        } catch (Throwable exception) {
            failure = exception;
        } finally {
            try {
                finisher.finish(run.getId(), workerId, execution, failure);
            } finally {
                activeRun.compareAndSet(run.getId(), null);
            }
        }
    }

    private JobExecution recoveredCompletion(BatchRun run) {
        if (run.getBatchExecutionId() == null) {
            return null;
        }
        JobExecution execution = jobRepository.getJobExecution(run.getBatchExecutionId());
        return execution != null && execution.getStatus() == BatchStatus.COMPLETED ? execution : null;
    }

    private void closeStaleExecution(BatchRun run) {
        if (run.getBatchExecutionId() == null) {
            return;
        }
        JobExecution stale = jobRepository.getJobExecution(run.getBatchExecutionId());
        if (stale == null || !stale.isRunning()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        stale.getStepExecutions().stream().filter(step -> step.getStatus().isRunning()).forEach(step -> {
            step.setStatus(BatchStatus.FAILED);
            step.setExitStatus(new ExitStatus("FAILED", "Worker lease expired"));
            step.setEndTime(now);
            jobRepository.update(step);
        });
        stale.setStatus(BatchStatus.FAILED);
        stale.setExitStatus(new ExitStatus("FAILED", "Worker lease expired"));
        stale.setEndTime(now);
        jobRepository.update(stale);
    }

    private JobParameters parameters(BatchRun run) {
        return new JobParametersBuilder()
                .addString("tenantKey", run.getTenantKey())
                .addString("siteKey", run.getSiteKey())
                .addString("definitionKey", run.getDefinitionKey())
                .addString("runKey", run.getRunKey())
                .toJobParameters();
    }

    private static String host() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception exception) {
            return "batch-worker";
        }
    }
}
