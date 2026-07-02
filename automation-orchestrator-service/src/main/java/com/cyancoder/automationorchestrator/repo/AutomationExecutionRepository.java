package com.cyancoder.automationorchestrator.repo;

import com.cyancoder.automationorchestrator.domain.AutomationExecution;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AutomationExecutionRepository extends MongoRepository<AutomationExecution, String> {
    Optional<AutomationExecution> findByExecutionId(String executionId);
    Optional<AutomationExecution> findFirstByTenantKeyAndSiteKeyAndIdempotencyKeyOrderByCreatedAtDesc(String tenantKey, String siteKey, String idempotencyKey);
}
