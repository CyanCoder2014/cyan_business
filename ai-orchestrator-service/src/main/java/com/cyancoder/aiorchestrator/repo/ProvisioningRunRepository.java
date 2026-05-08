package com.cyancoder.aiorchestrator.repo;

import com.cyancoder.aiorchestrator.domain.ProvisioningRun;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProvisioningRunRepository extends MongoRepository<ProvisioningRun, String> {
    List<ProvisioningRun> findByDraftIdOrderByStartedAtDesc(String draftId);
    Optional<ProvisioningRun> findByRunId(String runId);
}
