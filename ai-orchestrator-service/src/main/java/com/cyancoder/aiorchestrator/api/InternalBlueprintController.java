package com.cyancoder.aiorchestrator.api;

import com.cyancoder.aiorchestrator.domain.AppBlueprint;
import com.cyancoder.aiorchestrator.service.BlueprintCatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/ai-orchestrator/blueprints")
public class InternalBlueprintController {
    private final BlueprintCatalogService blueprintCatalogService;

    public InternalBlueprintController(BlueprintCatalogService blueprintCatalogService) {
        this.blueprintCatalogService = blueprintCatalogService;
    }

    @GetMapping
    public List<AppBlueprint> list() {
        return blueprintCatalogService.listActive();
    }

    @GetMapping("/{blueprintKey}")
    public AppBlueprint get(@PathVariable String blueprintKey) {
        return blueprintCatalogService.getActiveByBlueprintKey(blueprintKey);
    }
}
