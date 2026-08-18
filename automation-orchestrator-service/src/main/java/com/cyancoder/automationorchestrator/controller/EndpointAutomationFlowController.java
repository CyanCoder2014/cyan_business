package com.cyancoder.automationorchestrator.controller;

import com.cyancoder.automationorchestrator.domain.AutomationFlowDefinition;
import com.cyancoder.automationorchestrator.domain.AutomationNodeType;
import com.cyancoder.automationorchestrator.service.AutomationAiAuthorizationService;
import com.cyancoder.automationorchestrator.service.AutomationFlowDefinitionService;
import com.cyancoder.automationorchestrator.service.N8nWorkflowCompatibilityService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/endpoint/automation-flows")
public class EndpointAutomationFlowController {
    private static final String READ = "@platformAuthorizationService.hasAnyPermission('automation.read','automation.manage','builder:*')";
    private static final String MANAGE = "@platformAuthorizationService.hasAnyPermission('automation.manage','builder:*')";

    private final AutomationFlowDefinitionService service;
    private final N8nWorkflowCompatibilityService n8n;
    private final AutomationAiAuthorizationService aiAuthorization;

    public EndpointAutomationFlowController(AutomationFlowDefinitionService service,
                                            N8nWorkflowCompatibilityService n8n,
                                            AutomationAiAuthorizationService aiAuthorization) {
        this.service = service;
        this.n8n = n8n;
        this.aiAuthorization = aiAuthorization;
    }

    @PostMapping
    @PreAuthorize(MANAGE)
    public AutomationFlowDefinition save(@RequestHeader(value = "X-Tenant-Key", required = false) String tenant,
                                         @RequestHeader(value = "X-Site-Key", required = false) String site,
                                         @RequestBody AutomationFlowDefinition definition,
                                         Authentication authentication) {
        if (hasAi(definition)) aiAuthorization.requireBuilder(tenant, site, username(authentication));
        return service.save(tenant, site, definition, usernameOrSystem(authentication));
    }

    @GetMapping
    @PreAuthorize(READ)
    public List<AutomationFlowDefinition> list(@RequestHeader(value = "X-Tenant-Key", required = false) String tenant,
                                               @RequestHeader(value = "X-Site-Key", required = false) String site) {
        return service.list(tenant, site);
    }

    @GetMapping("/{flowKey}/versions/{version}")
    @PreAuthorize(READ)
    public AutomationFlowDefinition get(@RequestHeader(value = "X-Tenant-Key", required = false) String tenant,
                                        @RequestHeader(value = "X-Site-Key", required = false) String site,
                                        @PathVariable String flowKey,
                                        @PathVariable Integer version) {
        return service.get(tenant, site, flowKey, version);
    }

    @GetMapping("/{flowKey}/active")
    @PreAuthorize(READ)
    public AutomationFlowDefinition active(@RequestHeader(value = "X-Tenant-Key", required = false) String tenant,
                                           @RequestHeader(value = "X-Site-Key", required = false) String site,
                                           @PathVariable String flowKey,
                                           @RequestParam(required = false, defaultValue = "default") String environment) {
        return service.active(tenant, site, flowKey, environment);
    }

    @GetMapping("/{flowKey}/versions/{version}/readiness")
    @PreAuthorize(READ)
    public Map<String, Object> readiness(@RequestHeader(value = "X-Tenant-Key", required = false) String tenant,
                                         @RequestHeader(value = "X-Site-Key", required = false) String site,
                                         @PathVariable String flowKey,
                                         @PathVariable Integer version) {
        return service.readiness(tenant, site, flowKey, version);
    }

    @PostMapping("/{flowKey}/versions/{version}/{action}")
    @PreAuthorize(MANAGE)
    public AutomationFlowDefinition lifecycle(@RequestHeader(value = "X-Tenant-Key", required = false) String tenant,
                                              @RequestHeader(value = "X-Site-Key", required = false) String site,
                                              @PathVariable String flowKey,
                                              @PathVariable Integer version,
                                              @PathVariable String action,
                                              @RequestParam(required = false) String targetEnvironment,
                                              Authentication authentication) {
        AutomationFlowDefinition definition = service.get(tenant, site, flowKey, version);
        if (hasAi(definition)) aiAuthorization.requireBuilder(tenant, site, username(authentication));
        return service.lifecycle(tenant, site, flowKey, version, action, usernameOrSystem(authentication), targetEnvironment);
    }

    @PostMapping("/n8n/analyze")
    @PreAuthorize(MANAGE)
    public Map<String, Object> analyzeN8n(@RequestBody Map<String, Object> workflow) {
        return n8n.analyze(workflow);
    }

    @PostMapping("/n8n/import")
    @PreAuthorize(MANAGE)
    public AutomationFlowDefinition importN8n(@RequestHeader(value = "X-Tenant-Key", required = false) String tenant,
                                              @RequestHeader(value = "X-Site-Key", required = false) String site,
                                              @RequestParam(required = false) String flowKey,
                                              @RequestBody Map<String, Object> workflow,
                                              Authentication authentication) {
        return n8n.importAndSave(tenant, site, flowKey, workflow, usernameOrSystem(authentication));
    }

    @GetMapping("/{flowKey}/versions/{version}/n8n-export")
    @PreAuthorize(READ)
    public Map<String, Object> exportN8n(@RequestHeader(value = "X-Tenant-Key", required = false) String tenant,
                                        @RequestHeader(value = "X-Site-Key", required = false) String site,
                                        @PathVariable String flowKey,
                                        @PathVariable Integer version) {
        return n8n.export(service.get(tenant, site, flowKey, version));
    }

    private boolean hasAi(AutomationFlowDefinition definition) {
        return definition != null && definition.getNodes().stream()
                .anyMatch(node -> node.type() == AutomationNodeType.AI_OPERATION);
    }

    private String username(Authentication authentication) {
        return authentication == null ? null : authentication.getName();
    }

    private String usernameOrSystem(Authentication authentication) {
        return authentication == null ? "system" : authentication.getName();
    }
}
