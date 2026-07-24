package com.cyancoder.batchworker.service;

import com.cyancoder.batchworker.domain.BatchDefinition;
import com.cyancoder.batchworker.domain.BatchRun;
import com.cyancoder.batchworker.domain.BatchRunStatus;
import com.cyancoder.batchworker.repository.BatchRunRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BatchRunService {
    private final BatchRunRepository repository;
    private final BatchDefinitionService definitions;

    public BatchRunService(BatchRunRepository repository, BatchDefinitionService definitions) {
        this.repository = repository;
        this.definitions = definitions;
    }

    public BatchRun start(String tenant, String site, String definitionKey, String runKey) {
        BatchDefinition definition = definitions.get(tenant, site, definitionKey);
        if (!definition.isActive()) {
            throw new IllegalStateException("Batch definition is inactive: " + definitionKey);
        }
        return repository.findByTenantKeyAndSiteKeyAndDefinitionKeyAndRunKey(
                tenant, site, definitionKey, runKey).orElseGet(() -> create(tenant, site, definitionKey, runKey));
    }

    private BatchRun create(String tenant, String site, String definitionKey, String runKey) {
        BatchRun run = new BatchRun();
        run.setTenantKey(tenant);
        run.setSiteKey(site);
        run.setDefinitionKey(definitionKey);
        run.setRunKey(runKey);
        run.setStatus(BatchRunStatus.QUEUED);
        run.setCreatedAt(Instant.now());
        try {
            return repository.saveAndFlush(run);
        } catch (DataIntegrityViolationException duplicate) {
            return repository.findByTenantKeyAndSiteKeyAndDefinitionKeyAndRunKey(
                    tenant, site, definitionKey, runKey).orElseThrow(() -> duplicate);
        }
    }

    public BatchRun get(String tenant, String site, UUID id) {
        BatchRun run = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Batch run not found: " + id));
        if (!run.getTenantKey().equals(tenant) || !run.getSiteKey().equals(site)) {
            throw new IllegalArgumentException("Batch run not found: " + id);
        }
        return run;
    }

    public List<BatchRun> history(String tenant, String site, int limit) {
        return repository.findByTenantKeyAndSiteKeyOrderByCreatedAtDesc(
                tenant, site, PageRequest.of(0, Math.min(Math.max(limit, 1), 200)));
    }

    @Transactional
    public BatchRun retry(String tenant, String site, UUID id) {
        BatchRun run = get(tenant, site, id);
        if (run.getStatus() != BatchRunStatus.FAILED) {
            throw new IllegalStateException("Only failed runs can be retried");
        }
        run.setStatus(BatchRunStatus.QUEUED);
        run.setWorkerId(null);
        run.setLeaseUntil(null);
        run.setHeartbeatAt(null);
        run.setCompletedAt(null);
        run.setErrorMessage(null);
        return repository.save(run);
    }
}
