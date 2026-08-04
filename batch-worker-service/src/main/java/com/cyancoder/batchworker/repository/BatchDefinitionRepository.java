package com.cyancoder.batchworker.repository;

import com.cyancoder.batchworker.domain.BatchDefinition;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchDefinitionRepository extends JpaRepository<BatchDefinition, UUID> {
    Optional<BatchDefinition> findByTenantKeyAndSiteKeyAndDefinitionKey(
            String tenantKey, String siteKey, String definitionKey);
}
