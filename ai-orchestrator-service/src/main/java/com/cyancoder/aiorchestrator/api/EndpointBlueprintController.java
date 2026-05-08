package com.cyancoder.aiorchestrator.api;

import com.cyancoder.aiorchestrator.domain.AppBlueprint;
import com.cyancoder.aiorchestrator.service.BlueprintCatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/endpoint/ai-orchestrator/blueprints")
public class EndpointBlueprintController {
    private final BlueprintCatalogService blueprintCatalogService;

    public EndpointBlueprintController(BlueprintCatalogService blueprintCatalogService) {
        this.blueprintCatalogService = blueprintCatalogService;
    }

    @GetMapping
    public List<AppBlueprint> list(@RequestParam(value = "appType", required = false) String appType) {
        List<AppBlueprint> blueprints = blueprintCatalogService.listActive();
        if (appType == null || appType.isBlank()) {
            return blueprints;
        }
        return blueprints.stream().filter(item -> appType.equalsIgnoreCase(item.getAppType())).toList();
    }

    @GetMapping("/{blueprintKey}")
    public AppBlueprint get(@PathVariable String blueprintKey) {
        return blueprintCatalogService.getActiveByBlueprintKey(blueprintKey);
    }
}
