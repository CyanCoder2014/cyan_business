package com.cyancoder.bpm.repo;

import com.cyancoder.bpm.domain.DynamicFlowDefinition;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DynamicFlowDefinitionRepository extends MongoRepository<DynamicFlowDefinition, String> {
    List<DynamicFlowDefinition> findByTenantKeyAndSiteKeyOrderByFlowKeyAscVersionDesc(String tenantKey, String siteKey);
    Optional<DynamicFlowDefinition> findFirstByTenantKeyAndSiteKeyAndFlowKeyAndActiveTrueOrderByVersionDesc(String tenantKey, String siteKey, String flowKey);
    Optional<DynamicFlowDefinition> findFirstByTenantKeyAndSiteKeyAndFlowKeyOrderByVersionDesc(String tenantKey, String siteKey, String flowKey);
}

