package com.cyancoder.automationorchestrator.controller;

import com.cyancoder.automationorchestrator.domain.AutomationFlowDefinition;
import com.cyancoder.automationorchestrator.service.AutomationFlowDefinitionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/automation-flows")
public class InternalAutomationFlowController {
    private final AutomationFlowDefinitionService service;
    public InternalAutomationFlowController(AutomationFlowDefinitionService service) { this.service = service; }
    @PostMapping public AutomationFlowDefinition save(@RequestHeader(value="X-Tenant-Key", required=false) String tenant, @RequestHeader(value="X-Site-Key", required=false) String site, @RequestBody AutomationFlowDefinition definition) { return service.save(tenant, site, definition, "internal"); }
    @GetMapping public List<AutomationFlowDefinition> list(@RequestHeader(value="X-Tenant-Key", required=false) String tenant, @RequestHeader(value="X-Site-Key", required=false) String site) { return service.list(tenant, site); }
    @GetMapping("/{flowKey}/active") public AutomationFlowDefinition active(@RequestHeader(value="X-Tenant-Key", required=false) String tenant, @RequestHeader(value="X-Site-Key", required=false) String site, @PathVariable String flowKey, @RequestParam(required=false, defaultValue="default") String environment) { return service.active(tenant, site, flowKey, environment); }
    @GetMapping("/{flowKey}/versions/{version}") public AutomationFlowDefinition get(@RequestHeader(value="X-Tenant-Key", required=false) String tenant, @RequestHeader(value="X-Site-Key", required=false) String site, @PathVariable String flowKey, @PathVariable Integer version) { return service.get(tenant, site, flowKey, version); }
    @PostMapping("/{flowKey}/versions/{version}/{action}") public AutomationFlowDefinition lifecycle(@RequestHeader(value="X-Tenant-Key", required=false) String tenant, @RequestHeader(value="X-Site-Key", required=false) String site, @PathVariable String flowKey, @PathVariable Integer version, @PathVariable String action, @RequestParam(required=false) String targetEnvironment) { return service.lifecycle(tenant, site, flowKey, version, action, "internal", targetEnvironment); }
}
