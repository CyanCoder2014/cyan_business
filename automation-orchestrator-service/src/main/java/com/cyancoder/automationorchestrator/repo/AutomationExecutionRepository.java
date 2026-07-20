package com.cyancoder.automationorchestrator.repo;

import com.cyancoder.automationorchestrator.domain.AutomationExecution;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.time.Instant;
import java.util.List;

public interface AutomationExecutionRepository extends MongoRepository<AutomationExecution, String> {
    Optional<AutomationExecution> findByExecutionId(String executionId);
    Optional<AutomationExecution> findFirstByExecutionIdAndTenantKeyAndSiteKey(String executionId, String tenantKey, String siteKey);
    Optional<AutomationExecution> findFirstByTenantKeyAndSiteKeyAndIdempotencyKeyOrderByCreatedAtDesc(String tenantKey, String siteKey, String idempotencyKey);
    List<AutomationExecution> findAllByStatusInAndResumeAtLessThanEqual(List<String> statuses, Instant resumeAt);
    long countByStatusInAndCurrentNodeId(List<String> statuses, String currentNodeId);
    long countByStatusInAndCurrentNodeIdAndCurrentConcurrencyKey(List<String> statuses, String currentNodeId, String currentConcurrencyKey);
    List<AutomationExecution> findAllByTenantKeyAndSiteKey(String tenantKey, String siteKey);
}
