package com.cyancoder.bpm.api;

import com.cyancoder.bpm.api.dto.FlowScopeResolver;
import com.cyancoder.bpm.domain.DynamicFlowDefinition;
import com.cyancoder.bpm.service.FlowDefinitionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/bpm/flows")
public class InternalDynamicFlowDefinitionController {
    private final FlowDefinitionService flowDefinitionService;

    public InternalDynamicFlowDefinitionController(FlowDefinitionService flowDefinitionService) {
        this.flowDefinitionService = flowDefinitionService;
    }

    @GetMapping
    public List<DynamicFlowDefinition> list(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey
    ) {
        return flowDefinitionService.list(FlowScopeResolver.fromHeaders(tenantKey, siteKey));
    }

    @GetMapping("/{flowKey}")
    public DynamicFlowDefinition getLatest(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable String flowKey
    ) {
        return flowDefinitionService.getLatestByFlowKey(FlowScopeResolver.fromHeaders(tenantKey, siteKey), flowKey);
    }

    @PostMapping
    public DynamicFlowDefinition save(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @RequestBody DynamicFlowDefinition definition
    ) {
        return flowDefinitionService.save(FlowScopeResolver.fromHeaders(tenantKey, siteKey), definition);
    }
}

