package com.cyancoder.automationorchestrator.repo;

import com.cyancoder.automationorchestrator.domain.AutomationFlowDefinition;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AutomationFlowDefinitionRepository extends MongoRepository<AutomationFlowDefinition, String> {
    List<AutomationFlowDefinition> findAllByTenantKeyAndSiteKeyOrderByFlowKeyAscVersionDesc(String tenantKey, String siteKey);
    Optional<AutomationFlowDefinition> findFirstByTenantKeyAndSiteKeyAndFlowKeyAndVersion(String tenantKey, String siteKey, String flowKey, Integer version);
    Optional<AutomationFlowDefinition> findFirstByTenantKeyAndSiteKeyAndFlowKeyAndActiveTrueOrderByVersionDesc(String tenantKey, String siteKey, String flowKey);
    Optional<AutomationFlowDefinition> findFirstByTenantKeyAndSiteKeyAndFlowKeyAndEnvironmentAndActiveTrueOrderByVersionDesc(String tenantKey, String siteKey, String flowKey, String environment);
    Optional<AutomationFlowDefinition> findFirstByTenantKeyAndSiteKeyAndFlowKeyOrderByVersionDesc(String tenantKey, String siteKey, String flowKey);
    List<AutomationFlowDefinition> findAllByTenantKeyAndSiteKeyAndFlowKeyAndEnvironmentAndActiveTrue(String tenantKey, String siteKey, String flowKey, String environment);
}
