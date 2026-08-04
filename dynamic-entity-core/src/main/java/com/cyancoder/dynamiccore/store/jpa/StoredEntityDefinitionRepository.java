package com.cyancoder.dynamiccore.store.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoredEntityDefinitionRepository extends JpaRepository<StoredEntityDefinition, Long> {
    Optional<StoredEntityDefinition> findByServiceKeyAndTenantKeyAndSiteKeyAndEntityKey(String serviceKey, String tenantKey, String siteKey, String entityKey);
    Page<StoredEntityDefinition> findByServiceKeyAndTenantKeyAndSiteKey(
            String serviceKey, String tenantKey, String siteKey, Pageable pageable);
    void deleteByServiceKeyAndTenantKeyAndSiteKeyAndEntityKey(String serviceKey, String tenantKey, String siteKey, String entityKey);
}
