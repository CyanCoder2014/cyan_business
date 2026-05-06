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

    public FlowDefinitionService(DynamicFlowDefinitionRepository repository) {
        this.repository = repository;
    }

    public DynamicFlowDefinition save(BpmScope scope, DynamicFlowDefinition definition) {
        if (definition.getVersion() == null || definition.getVersion() < 1) {
            Integer latestVersion = repository.findFirstByTenantKeyAndSiteKeyAndFlowKeyOrderByVersionDesc(scope.tenantKey(), scope.siteKey(), definition.getFlowKey())
                    .map(DynamicFlowDefinition::getVersion)
                    .orElse(0);
            definition.setVersion(latestVersion + 1);
        }
        definition.setTenantKey(scope.tenantKey());
        definition.setSiteKey(scope.siteKey());
        definition.setUpdatedAt(Instant.now());
        return repository.save(definition);
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
}

