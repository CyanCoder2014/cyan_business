package com.cyancoder.aiorchestrator.service;

import com.cyancoder.aiorchestrator.domain.AppBlueprint;

import java.util.List;

public interface BlueprintCatalogService {
    List<AppBlueprint> listActive();
    AppBlueprint getActiveByBlueprintKey(String blueprintKey);
    AppBlueprint resolveActiveByType(String appType);
}
