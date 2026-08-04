package com.cyancoder.batchworker.service;

import com.cyancoder.batchworker.config.BatchWorkerProperties;
import com.cyancoder.batchworker.domain.BatchRun;
import com.cyancoder.batchworker.domain.BatchRunStatus;
import com.cyancoder.batchworker.repository.BatchRunRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BatchRunClaimService {
    public record Claim(BatchRun run, boolean recovery) {}

    private final BatchRunRepository repository;
    private final BatchWorkerProperties properties;

    public BatchRunClaimService(BatchRunRepository repository, BatchWorkerProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @Transactional
    public Optional<Claim> claim(String workerId) {
        Instant now = Instant.now();
        Optional<BatchRun> expired = repository.lockExpired(now, PageRequest.of(0, 1)).stream().findFirst();
        if (expired.isPresent()) {
            return Optional.of(lease(expired.get(), workerId, now, true));
        }
        return repository.lockNextByStatus(BatchRunStatus.QUEUED, PageRequest.of(0, 1)).stream()
                .findFirst().map(run -> lease(run, workerId, now, false));
    }

    private Claim lease(BatchRun run, String workerId, Instant now, boolean recovery) {
        run.setStatus(BatchRunStatus.RUNNING);
        run.setWorkerId(workerId);
        run.setHeartbeatAt(now);
        run.setLeaseUntil(now.plus(properties.getLeaseDuration()));
        if (run.getStartedAt() == null) {
            run.setStartedAt(now);
        }
        return new Claim(repository.save(run), recovery);
    }

    @Transactional
    public void renew(UUID runId, String workerId) {
        Instant now = Instant.now();
        repository.renewLease(runId, workerId, now, now.plus(properties.getLeaseDuration()));
    }
}
