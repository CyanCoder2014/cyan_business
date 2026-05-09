package com.cyancoder.automationorchestrator.controller;

import com.cyancoder.automationorchestrator.model.AutomationStartRequest;
import com.cyancoder.automationorchestrator.model.AutomationStartResponse;
import com.cyancoder.automationorchestrator.service.AutomationExecutionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/automation-orchestrator")
public class InternalAutomationExecutionController {
    private final AutomationExecutionService automationExecutionService;

    public InternalAutomationExecutionController(AutomationExecutionService automationExecutionService) {
        this.automationExecutionService = automationExecutionService;
    }

    @PostMapping("/executions/start")
    public AutomationStartResponse start(@RequestBody AutomationStartRequest request) {
        return automationExecutionService.start(request);
    }
}
