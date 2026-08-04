package com.cyancoder.automationorchestrator.controller;

import com.cyancoder.automationorchestrator.domain.AutomationFlowDefinition;
import com.cyancoder.automationorchestrator.service.AutomationFlowDefinitionService;
import com.cyancoder.automationorchestrator.service.N8nWorkflowCompatibilityService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/endpoint/automation-flows")
@PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')")
public class EndpointAutomationFlowController {
    private final AutomationFlowDefinitionService service;
    private final N8nWorkflowCompatibilityService n8n;
    public EndpointAutomationFlowController(AutomationFlowDefinitionService service, N8nWorkflowCompatibilityService n8n) { this.service = service; this.n8n = n8n; }
    @PostMapping public AutomationFlowDefinition save(@RequestHeader(value="X-Tenant-Key", required=false) String tenant, @RequestHeader(value="X-Site-Key", required=false) String site, @RequestBody AutomationFlowDefinition definition, Authentication auth) { return service.save(tenant, site, definition, auth == null ? "system" : auth.getName()); }
    @GetMapping public List<AutomationFlowDefinition> list(@RequestHeader(value="X-Tenant-Key", required=false) String tenant, @RequestHeader(value="X-Site-Key", required=false) String site) { return service.list(tenant, site); }
    @GetMapping("/{flowKey}/versions/{version}") public AutomationFlowDefinition get(@RequestHeader(value="X-Tenant-Key", required=false) String tenant, @RequestHeader(value="X-Site-Key", required=false) String site, @PathVariable String flowKey, @PathVariable Integer version) { return service.get(tenant, site, flowKey, version); }
    @GetMapping("/{flowKey}/active") public AutomationFlowDefinition active(@RequestHeader(value="X-Tenant-Key", required=false) String tenant, @RequestHeader(value="X-Site-Key", required=false) String site, @PathVariable String flowKey, @RequestParam(required=false, defaultValue="default") String environment) { return service.active(tenant, site, flowKey, environment); }
    @PostMapping("/{flowKey}/versions/{version}/{action}") public AutomationFlowDefinition lifecycle(@RequestHeader(value="X-Tenant-Key", required=false) String tenant, @RequestHeader(value="X-Site-Key", required=false) String site, @PathVariable String flowKey, @PathVariable Integer version, @PathVariable String action, @RequestParam(required=false) String targetEnvironment, Authentication auth) { return service.lifecycle(tenant, site, flowKey, version, action, auth == null ? "system" : auth.getName(), targetEnvironment); }
    @PostMapping("/n8n/analyze") public Map<String,Object> analyzeN8n(@RequestBody Map<String,Object> workflow) { return n8n.analyze(workflow); }
    @PostMapping("/n8n/import") public AutomationFlowDefinition importN8n(@RequestHeader(value="X-Tenant-Key", required=false) String tenant, @RequestHeader(value="X-Site-Key", required=false) String site, @RequestParam(required=false) String flowKey, @RequestBody Map<String,Object> workflow, Authentication auth) { return n8n.importAndSave(tenant, site, flowKey, workflow, auth == null ? "system" : auth.getName()); }
    @GetMapping("/{flowKey}/versions/{version}/n8n-export") public Map<String,Object> exportN8n(@RequestHeader(value="X-Tenant-Key", required=false) String tenant, @RequestHeader(value="X-Site-Key", required=false) String site, @PathVariable String flowKey, @PathVariable Integer version) { return n8n.export(service.get(tenant, site, flowKey, version)); }
}
