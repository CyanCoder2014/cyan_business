package com.cyancoder.dynamiccore.store.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoredEntityDefinitionRepository extends JpaRepository<StoredEntityDefinition, Long> {
    Optional<StoredEntityDefinition> findByServiceKeyAndTenantKeyAndSiteKeyAndEntityKey(String serviceKey, String tenantKey, String siteKey, String entityKey);
    List<StoredEntityDefinition> findByServiceKeyAndTenantKeyAndSiteKeyOrderByEntityKeyAsc(String serviceKey, String tenantKey, String siteKey);
    void deleteByServiceKeyAndTenantKeyAndSiteKeyAndEntityKey(String serviceKey, String tenantKey, String siteKey, String entityKey);
}
