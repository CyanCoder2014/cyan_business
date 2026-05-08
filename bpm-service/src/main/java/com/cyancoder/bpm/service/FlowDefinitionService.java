package com.cyancoder.bpm.service;

import com.cyancoder.bpm.api.dto.BpmScope;
import com.cyancoder.bpm.domain.DynamicFlowDefinition;
import com.cyancoder.bpm.repo.DynamicFlowDefinitionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class FlowDefinitionService {
    private final DynamicFlowDefinitionRepository repository;
    private final DynamicFlowDeploymentService deploymentService;

    public FlowDefinitionService(DynamicFlowDefinitionRepository repository,
                                 DynamicFlowDeploymentService deploymentService) {
        this.repository = repository;
        this.deploymentService = deploymentService;
    }

    public DynamicFlowDefinition save(BpmScope scope, DynamicFlowDefinition definition) {
        if (definition.getVersion() == null || definition.getVersion() < 1) {
            Integer latestVersion = repository.findFirstByTenantKeyAndSiteKeyAndFlowKeyOrderByVersionDesc(scope.tenantKey(), scope.siteKey(), definition.getFlowKey())
                    .map(DynamicFlowDefinition::getVersion)
                    .orElse(0);
            definition.setVersion(latestVersion + 1);
        }
        if (definition.isActive()) {
            deactivateOthers(scope, definition.getFlowKey(), definition.getVersion());
        }
        definition.setTenantKey(scope.tenantKey());
        definition.setSiteKey(scope.siteKey());
        definition.setUpdatedAt(Instant.now());
        DynamicFlowDefinition saved = repository.save(definition);
        if (saved.isActive() && deploymentService.isAvailable()) {
            deploymentService.deploy(saved);
        }
        return saved;
    }

    public List<DynamicFlowDefinition> list(BpmScope scope) {
        return repository.findByTenantKeyAndSiteKeyOrderByFlowKeyAscVersionDesc(scope.tenantKey(), scope.siteKey());
    }

    public DynamicFlowDefinition getActiveByFlowKey(BpmScope scope, String flowKey) {
        return repository.findFirstByTenantKeyAndSiteKeyAndFlowKeyAndActiveTrueOrderByVersionDesc(scope.tenantKey(), scope.siteKey(), flowKey)
                .orElseThrow();
    }

    public DynamicFlowDefinition getLatestByFlowKey(BpmScope scope, String flowKey) {
        return repository.findFirstByTenantKeyAndSiteKeyAndFlowKeyOrderByVersionDesc(scope.tenantKey(), scope.siteKey(), flowKey)
                .orElseThrow();
    }

    public DynamicFlowDefinition activate(BpmScope scope, String flowKey, Integer version) {
        DynamicFlowDefinition definition = repository.findByTenantKeyAndSiteKeyAndFlowKeyAndVersion(scope.tenantKey(), scope.siteKey(), flowKey, version)
                .orElseThrow();
        deactivateOthers(scope, flowKey, version);
        definition.setActive(true);
        definition.setUpdatedAt(Instant.now());
        DynamicFlowDefinition saved = repository.save(definition);
        if (deploymentService.isAvailable()) {
            deploymentService.deploy(saved);
        }
        return saved;
    }

    private void deactivateOthers(BpmScope scope, String flowKey, Integer keepVersion) {
        for (DynamicFlowDefinition existing : repository.findByTenantKeyAndSiteKeyAndFlowKeyOrderByVersionDesc(scope.tenantKey(), scope.siteKey(), flowKey)) {
            if (keepVersion != null && keepVersion.equals(existing.getVersion())) {
                continue;
            }
            if (existing.isActive()) {
                existing.setActive(false);
                existing.setUpdatedAt(Instant.now());
                repository.save(existing);
            }
        }
    }
}
