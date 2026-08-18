package com.cyancoder.bpm.api;

import com.cyancoder.bpm.api.dto.BpmScope;
import com.cyancoder.bpm.api.dto.FlowScopeResolver;
import com.cyancoder.bpm.domain.DynamicFlowDefinition;
import com.cyancoder.bpm.service.FlowDefinitionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/endpoint/bpm/flows")
public class EndpointDynamicFlowDefinitionController {
    private final FlowDefinitionService flowDefinitionService;

    public EndpointDynamicFlowDefinitionController(FlowDefinitionService flowDefinitionService) {
        this.flowDefinitionService = flowDefinitionService;
    }

    @GetMapping
    @PreAuthorize("@platformAuthorizationService.hasAnyPermission('bpm.read','bpm.manage','builder:*')")
    public List<DynamicFlowDefinition> list(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey
    ) {
        return flowDefinitionService.list(FlowScopeResolver.fromHeaders(tenantKey, siteKey));
    }

    @GetMapping("/{flowKey}")
    @PreAuthorize("@platformAuthorizationService.hasAnyPermission('bpm.read','bpm.manage','builder:*')")
    public DynamicFlowDefinition getLatest(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable String flowKey
    ) {
        return flowDefinitionService.getLatestByFlowKey(FlowScopeResolver.fromHeaders(tenantKey, siteKey), flowKey);
    }

    @PostMapping
    @PreAuthorize("@platformAuthorizationService.hasAnyPermission('bpm.manage','builder:*')")
    public DynamicFlowDefinition save(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @RequestBody DynamicFlowDefinition definition
    ) {
        BpmScope scope = FlowScopeResolver.fromHeaders(tenantKey, siteKey);
        return flowDefinitionService.save(scope, definition);
    }

    @PostMapping("/{flowKey}/activate/{version}")
    @PreAuthorize("@platformAuthorizationService.hasAnyPermission('bpm.manage','builder:*')")
    public DynamicFlowDefinition activate(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable String flowKey,
            @PathVariable Integer version
    ) {
        return flowDefinitionService.activate(FlowScopeResolver.fromHeaders(tenantKey, siteKey), flowKey, version);
    }
}
