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

@RestController
@RequestMapping("/endpoint/automation-orchestrator")
public class EndpointAutomationExecutionController {
    private final AutomationExecutionService automationExecutionService;

    public EndpointAutomationExecutionController(AutomationExecutionService automationExecutionService) {
        this.automationExecutionService = automationExecutionService;
    }

    @PostMapping("/executions/start")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')")
    public AutomationStartResponse start(@RequestBody AutomationStartRequest request) {
        return automationExecutionService.start(request);
    }

    @GetMapping("/executions/{executionId}")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')")
    public AutomationStartResponse get(@PathVariable String executionId) {
        return automationExecutionService.get(executionId);
    }

    @PostMapping("/executions/{executionId}/cancel")
    @PreAuthorize("@platformAuthorizationService.canUseCapability('operations:*')")
    public AutomationStartResponse cancel(@PathVariable String executionId) {
        return automationExecutionService.cancel(executionId);
    }
}
