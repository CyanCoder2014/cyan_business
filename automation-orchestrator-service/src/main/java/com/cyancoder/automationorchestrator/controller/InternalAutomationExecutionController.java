package com.cyancoder.automationorchestrator.controller;

import com.cyancoder.automationorchestrator.model.AutomationStartRequest;
import com.cyancoder.automationorchestrator.model.AutomationStartResponse;
import com.cyancoder.automationorchestrator.service.AutomationExecutionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Map;
import com.cyancoder.automationorchestrator.domain.AutomationExecutionStep;

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

    @GetMapping("/executions/{executionId}")
    public AutomationStartResponse get(@PathVariable String executionId) {
        return automationExecutionService.get(executionId);
    }

    @PostMapping("/executions/{executionId}/cancel")
    public AutomationStartResponse cancel(@PathVariable String executionId) {
        return automationExecutionService.cancel(executionId);
    }

    @GetMapping("/executions/{executionId}/steps") public List<AutomationExecutionStep> steps(@PathVariable String executionId) { return automationExecutionService.steps(executionId); }
    @GetMapping("/executions/{executionId}/dead-letters") public List<Map<String,Object>> deadLetters(@PathVariable String executionId) { return automationExecutionService.deadLetters(executionId); }
    @PostMapping("/executions/{executionId}/dead-letters/{deadLetterId}/requeue") public AutomationStartResponse requeue(@PathVariable String executionId,@PathVariable String deadLetterId){return automationExecutionService.requeueDeadLetter(executionId,deadLetterId);}
    @GetMapping("/metrics") public Map<String,Object> metrics(){return automationExecutionService.metrics();}
    @GetMapping("/executions") public List<com.cyancoder.automationorchestrator.model.AutomationStartResponse> history(@RequestHeader(value="X-Tenant-Key", required=false) String tenant,@RequestHeader(value="X-Site-Key", required=false) String site,@RequestParam(required=false) String flowKey,@RequestParam(required=false) String status){return automationExecutionService.history(tenant,site,flowKey,status);}
    @PostMapping("/executions/{executionId}/retry") public AutomationStartResponse retry(@PathVariable String executionId,@RequestHeader(value="X-Tenant-Key", required=false) String tenant,@RequestHeader(value="X-Site-Key", required=false) String site,@RequestParam(defaultValue="false") boolean fromFailedNode){return automationExecutionService.retry(executionId,tenant,site,fromFailedNode);}
}
