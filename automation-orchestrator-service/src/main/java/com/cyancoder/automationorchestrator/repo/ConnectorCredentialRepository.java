package com.cyancoder.automationorchestrator.repo;

import com.cyancoder.automationorchestrator.domain.ConnectorCredential;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ConnectorCredentialRepository extends MongoRepository<ConnectorCredential, String> {
    List<ConnectorCredential> findAllByTenantKeyAndSiteKeyOrderByNameAsc(String tenantKey, String siteKey);
    Optional<ConnectorCredential> findFirstByIdAndTenantKeyAndSiteKeyAndActiveTrue(String id, String tenantKey, String siteKey);
}
