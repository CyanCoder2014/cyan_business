package com.cyancoder.automationorchestrator.controller;

import com.cyancoder.automationorchestrator.domain.AutomationExecutionStep;
import com.cyancoder.automationorchestrator.model.AutomationStartRequest;
import com.cyancoder.automationorchestrator.model.AutomationStartResponse;
import com.cyancoder.automationorchestrator.service.AutomationAiAuthorizationService;
import com.cyancoder.automationorchestrator.service.AutomationExecutionService;
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
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/endpoint/automation-orchestrator")
public class EndpointAutomationExecutionController {
    private static final String READ = "@platformAuthorizationService.hasAnyPermission('automation.read','automation.execute','operations:*')";
    private static final String EXECUTE = "@platformAuthorizationService.hasAnyPermission('automation.execute','operations:*')";

    private final AutomationExecutionService automationExecutionService;
    private final AutomationAiAuthorizationService aiAuthorization;

    public EndpointAutomationExecutionController(AutomationExecutionService automationExecutionService,
                                                 AutomationAiAuthorizationService aiAuthorization) {
        this.automationExecutionService = automationExecutionService;
        this.aiAuthorization = aiAuthorization;
    }

    @PostMapping("/executions/start")
    @PreAuthorize(EXECUTE)
    public AutomationStartResponse start(@RequestBody AutomationStartRequest request,
                                         @RequestHeader(value = "X-Tenant-Key", required = false) String tenant,
                                         @RequestHeader(value = "X-Site-Key", required = false) String site,
                                         Authentication authentication) {
        if (automationExecutionService.requiresAi(request, tenant, site)) {
            aiAuthorization.requireExecution(tenant, site, username(authentication));
        }
        return automationExecutionService.startAuthorized(request, authorities(authentication), tenant, site, username(authentication));
    }

    @GetMapping("/executions/{executionId}")
    @PreAuthorize(READ)
    public AutomationStartResponse get(@PathVariable String executionId,
                                       @RequestHeader(value = "X-Tenant-Key", required = false) String tenant,
                                       @RequestHeader(value = "X-Site-Key", required = false) String site) {
        return automationExecutionService.get(executionId, tenant, site);
    }

    @PostMapping("/executions/{executionId}/cancel")
    @PreAuthorize(EXECUTE)
    public AutomationStartResponse cancel(@PathVariable String executionId,
                                          @RequestHeader(value = "X-Tenant-Key", required = false) String tenant,
                                          @RequestHeader(value = "X-Site-Key", required = false) String site) {
        return automationExecutionService.cancel(executionId, tenant, site);
    }

    @GetMapping("/executions/{executionId}/steps")
    @PreAuthorize(READ)
    public List<AutomationExecutionStep> steps(@PathVariable String executionId,
                                               @RequestHeader(value = "X-Tenant-Key", required = false) String tenant,
                                               @RequestHeader(value = "X-Site-Key", required = false) String site) {
        return automationExecutionService.steps(executionId, tenant, site);
    }

    @GetMapping("/executions/{executionId}/dead-letters")
    @PreAuthorize(READ)
    public List<Map<String, Object>> deadLetters(@PathVariable String executionId,
                                                 @RequestHeader(value = "X-Tenant-Key", required = false) String tenant,
                                                 @RequestHeader(value = "X-Site-Key", required = false) String site) {
        return automationExecutionService.deadLetters(executionId, tenant, site);
    }

    @PostMapping("/executions/{executionId}/dead-letters/{deadLetterId}/requeue")
    @PreAuthorize(EXECUTE)
    public AutomationStartResponse requeue(@PathVariable String executionId,
                                           @PathVariable String deadLetterId,
                                           @RequestHeader(value = "X-Tenant-Key", required = false) String tenant,
                                           @RequestHeader(value = "X-Site-Key", required = false) String site) {
        return automationExecutionService.requeueDeadLetter(executionId, deadLetterId, tenant, site);
    }

    @GetMapping("/metrics")
    @PreAuthorize(READ)
    public Map<String, Object> metrics(@RequestHeader(value = "X-Tenant-Key", required = false) String tenant,
                                       @RequestHeader(value = "X-Site-Key", required = false) String site) {
        return automationExecutionService.metrics(tenant, site);
    }

    @GetMapping("/executions")
    @PreAuthorize(READ)
    public List<AutomationStartResponse> history(@RequestHeader(value = "X-Tenant-Key", required = false) String tenant,
                                                 @RequestHeader(value = "X-Site-Key", required = false) String site,
                                                 @RequestParam(required = false) String flowKey,
                                                 @RequestParam(required = false) String status) {
        return automationExecutionService.history(tenant, site, flowKey, status);
    }

    @PostMapping("/executions/{executionId}/retry")
    @PreAuthorize(EXECUTE)
    public AutomationStartResponse retry(@PathVariable String executionId,
                                         @RequestHeader(value = "X-Tenant-Key", required = false) String tenant,
                                         @RequestHeader(value = "X-Site-Key", required = false) String site,
                                         @RequestParam(defaultValue = "false") boolean fromFailedNode) {
        return automationExecutionService.retry(executionId, tenant, site, fromFailedNode);
    }

    @PostMapping("/flows/{flowKey}/manual-run")
    @PreAuthorize(EXECUTE)
    public AutomationStartResponse manualRun(@PathVariable String flowKey,
                                             @RequestParam(required = false) Integer version,
                                             @RequestHeader(value = "X-Tenant-Key", required = false) String tenant,
                                             @RequestHeader(value = "X-Site-Key", required = false) String site,
                                             @RequestBody(required = false) Map<String, Object> request,
                                             Authentication authentication) {
        Map<String, Object> body = request == null ? Map.of() : request;
        if (automationExecutionService.requiresAiFlow(tenant, site, flowKey, version, body)) {
            aiAuthorization.requireExecution(tenant, site, username(authentication));
        }
        return automationExecutionService.manualRun(tenant, site, flowKey, version, body,
                authorities(authentication), username(authentication));
    }

    private String username(Authentication authentication) {
        return authentication == null ? null : authentication.getName();
    }

    private Set<String> authorities(Authentication authentication) {
        return authentication == null ? Set.of() : authentication.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toSet());
    }
}
