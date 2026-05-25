package com.cyancoder.aiorchestrator.service.impl;

import com.cyancoder.aiorchestrator.domain.AppBlueprint;
import com.cyancoder.aiorchestrator.repo.AppBlueprintRepository;
import com.cyancoder.aiorchestrator.service.BlueprintCatalogService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MongoBlueprintCatalogService implements BlueprintCatalogService {
    private final AppBlueprintRepository repository;
    private final SeedBlueprintFactory seedBlueprintFactory;

    public MongoBlueprintCatalogService(AppBlueprintRepository repository,
                                        SeedBlueprintFactory seedBlueprintFactory) {
        this.repository = repository;
        this.seedBlueprintFactory = seedBlueprintFactory;
    }

    @Override
    public List<AppBlueprint> listActive() {
        ensureSeeded();
        return repository.findByActiveTrueOrderByAppTypeAscVersionDesc();
    }

    @Override
    public AppBlueprint getActiveByBlueprintKey(String blueprintKey) {
        ensureSeeded();
        return repository.findFirstByBlueprintKeyAndActiveTrueOrderByVersionDesc(blueprintKey).orElseThrow();
    }

    @Override
    public AppBlueprint resolveActiveByType(String appType) {
        ensureSeeded();
        return repository.findFirstByAppTypeAndActiveTrueOrderByVersionDesc(normalizeAppType(appType)).orElseThrow();
    }

    private void ensureSeeded() {
        if (repository.count() > 0) {
            return;
        }
        repository.saveAll(seedBlueprintFactory.createDefaults());
    }

    private String normalizeAppType(String appType) {
        return appType == null ? "" : appType.trim().toLowerCase();
    }
}
