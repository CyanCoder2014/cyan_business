package com.cyancoder.automationorchestrator.controller;

import com.cyancoder.automationorchestrator.model.AutomationStartRequest;
import com.cyancoder.automationorchestrator.model.AutomationStartResponse;
import com.cyancoder.automationorchestrator.service.AutomationExecutionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.List;
import java.util.Map;
import com.cyancoder.automationorchestrator.domain.AutomationExecutionStep;
import org.springframework.security.core.Authentication;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/endpoint/automation-orchestrator")
public class EndpointAutomationExecutionController {
    private final AutomationExecutionService automationExecutionService;

    public EndpointAutomationExecutionController(AutomationExecutionService automationExecutionService) {
        this.automationExecutionService = automationExecutionService;
    }

    @PostMapping("/executions/start")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')")
    public AutomationStartResponse start(@RequestBody AutomationStartRequest request,
                                         @RequestHeader(value="X-Tenant-Key", required=false) String tenant,
                                         @RequestHeader(value="X-Site-Key", required=false) String site,
                                         Authentication authentication) {
        return automationExecutionService.startAuthorized(request, authentication == null ? java.util.Set.of() : authentication.getAuthorities().stream().map(item -> item.getAuthority()).collect(Collectors.toSet()), tenant, site);
    }

    @GetMapping("/executions/{executionId}")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')")
    public AutomationStartResponse get(@PathVariable String executionId,
                                       @RequestHeader(value="X-Tenant-Key", required=false) String tenant,
                                       @RequestHeader(value="X-Site-Key", required=false) String site) {
        return automationExecutionService.get(executionId, tenant, site);
    }

    @PostMapping("/executions/{executionId}/cancel")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')")
    public AutomationStartResponse cancel(@PathVariable String executionId,
                                          @RequestHeader(value="X-Tenant-Key", required=false) String tenant,
                                          @RequestHeader(value="X-Site-Key", required=false) String site) {
        return automationExecutionService.cancel(executionId, tenant, site);
    }

    @GetMapping("/executions/{executionId}/steps")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')")
    public List<AutomationExecutionStep> steps(@PathVariable String executionId, @RequestHeader(value="X-Tenant-Key", required=false) String tenant, @RequestHeader(value="X-Site-Key", required=false) String site) { return automationExecutionService.steps(executionId, tenant, site); }

    @GetMapping("/executions/{executionId}/dead-letters")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')")
    public List<Map<String,Object>> deadLetters(@PathVariable String executionId, @RequestHeader(value="X-Tenant-Key", required=false) String tenant, @RequestHeader(value="X-Site-Key", required=false) String site) { return automationExecutionService.deadLetters(executionId, tenant, site); }
    @PostMapping("/executions/{executionId}/dead-letters/{deadLetterId}/requeue") @PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')") public AutomationStartResponse requeue(@PathVariable String executionId,@PathVariable String deadLetterId,@RequestHeader(value="X-Tenant-Key", required=false) String tenant,@RequestHeader(value="X-Site-Key", required=false) String site){return automationExecutionService.requeueDeadLetter(executionId,deadLetterId,tenant,site);}
    @GetMapping("/metrics") @PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')") public Map<String,Object> metrics(@RequestHeader(value="X-Tenant-Key", required=false) String tenant,@RequestHeader(value="X-Site-Key", required=false) String site){return automationExecutionService.metrics(tenant,site);}
}
