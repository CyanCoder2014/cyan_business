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
        validate(definition);
        if (definition.getVersion() == null || definition.getVersion() < 1) {
            Integer latestVersion = repository.findFirstByTenantKeyAndSiteKeyAndFlowKeyOrderByVersionDesc(scope.tenantKey(), scope.siteKey(), definition.getFlowKey())
                    .map(DynamicFlowDefinition::getVersion)
                    .orElse(0);
            definition.setVersion(latestVersion + 1);
        }
        if (definition.isActive()) {
            deactivateOthers(scope, definition.getFlowKey(), definition.getVersion());
        }
        if (definition.getLifecycleStatus() == null || definition.getLifecycleStatus().isBlank()) definition.setLifecycleStatus("DRAFT");
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
        definition.setLifecycleStatus("ACTIVE");
        definition.setUpdatedAt(Instant.now());
        DynamicFlowDefinition saved = repository.save(definition);
        if (deploymentService.isAvailable()) {
            deploymentService.deploy(saved);
        }
        return saved;
    }

    public List<String> validate(DynamicFlowDefinition definition) {
        java.util.ArrayList<String> errors = new java.util.ArrayList<>();
        if (definition.getFlowKey() == null || definition.getFlowKey().isBlank()) errors.add("flowKey is required");
        if (definition.getStates() == null || definition.getStates().isEmpty()) errors.add("states are required");
        java.util.Set<String> ids = new java.util.HashSet<>();
        if (definition.getStates() != null) definition.getStates().forEach(state -> { if (state.id() == null || state.id().isBlank()) errors.add("every state requires an id"); else if (!ids.add(state.id())) errors.add("duplicate state id: " + state.id()); });
        if (definition.getStartState() == null || !ids.contains(definition.getStartState())) errors.add("startState must reference a state");
        if (definition.getTransitions() != null) definition.getTransitions().forEach(transition -> { if (!ids.contains(transition.fromState()) || !ids.contains(transition.toState())) errors.add("transition " + transition.id() + " references an unknown state"); });
        if (definition.getStates() != null && definition.getStates().stream().noneMatch(com.cyancoder.bpm.domain.FlowState::terminal)) errors.add("at least one terminal state is required");
        if (!errors.isEmpty()) throw new IllegalArgumentException(String.join("; ", errors));
        return errors;
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
