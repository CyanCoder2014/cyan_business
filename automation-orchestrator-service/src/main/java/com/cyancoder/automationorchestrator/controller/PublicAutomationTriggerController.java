package com.cyancoder.automationorchestrator.controller;

import com.cyancoder.automationorchestrator.config.AutomationCallbackProperties;
import com.cyancoder.automationorchestrator.domain.AutomationFlowDefinition;
import com.cyancoder.automationorchestrator.domain.AutomationNode;
import com.cyancoder.automationorchestrator.domain.AutomationNodeType;
import com.cyancoder.automationorchestrator.model.AutomationNodeCallbackRequest;
import com.cyancoder.automationorchestrator.model.AutomationStartResponse;
import com.cyancoder.automationorchestrator.service.AutomationExecutionService;
import com.cyancoder.automationorchestrator.service.AutomationFlowDefinitionService;
import com.cyancoder.platform.error.PlatformErrorCode;
import com.cyancoder.platform.error.PlatformServiceException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@RestController
@RequestMapping("/public/automation-orchestrator")
public class PublicAutomationTriggerController {
    private final AutomationExecutionService executions;
    private final AutomationFlowDefinitionService flows;
    private final AutomationCallbackProperties callbackProperties;
    public PublicAutomationTriggerController(AutomationExecutionService executions, AutomationFlowDefinitionService flows, AutomationCallbackProperties callbackProperties) { this.executions=executions;this.flows=flows;this.callbackProperties=callbackProperties; }

    @PostMapping("/webhooks/{flowKey}")
    public AutomationStartResponse webhook(@PathVariable String flowKey,
                                           @RequestHeader(value="X-Tenant-Key", required=false) String tenant,
                                           @RequestHeader(value="X-Site-Key", required=false) String site,
                                           @RequestHeader(value="X-Automation-Environment", required=false, defaultValue="default") String environment,
                                           @RequestHeader(value="X-Webhook-Secret", required=false) String secret,
                                           @RequestHeader(value="Idempotency-Key", required=false) String idempotencyKey,
                                           @RequestBody(required=false) Map<String,Object> payload) {
        AutomationFlowDefinition definition=flows.active(tenant,site,flowKey,environment);
        AutomationNode entry=definition.getNodes().stream().filter(node->definition.getEntryNodeId().equals(node.id())).findFirst().orElseThrow();
        if (entry.type() != AutomationNodeType.WEBHOOK_TRIGGER) {
            throw unauthorized("flow is not configured for public webhook execution");
        }
        Object expected=entry.configOrEmpty().get("webhookSecret");
        if(expected!=null&&!secureEquals(expected.toString(),secret)) {
            throw unauthorized("invalid webhook secret");
        }
        return executions.triggerWebhook(flowKey,tenant,site,payload==null?Map.of():payload,Map.of("entryType","WEBHOOK","environment",environment),idempotencyKey);
    }

    @PostMapping("/executions/{executionId}/nodes/{nodeId}/callback")
    public AutomationStartResponse callback(@PathVariable String executionId,@PathVariable String nodeId,
                                            @RequestHeader(value="X-Automation-Callback-Secret", required=false) String secret,
                                            @RequestBody(required=false) AutomationNodeCallbackRequest request) {
        if(!secureEquals(callbackProperties.getSecret(),secret)) {
            throw unauthorized("invalid automation callback secret");
        }
        return executions.acceptCallback(executionId,nodeId,request==null?null:request.callbackId(),request==null?Map.of():request.payload());
    }
    private boolean secureEquals(String expected,String actual){return expected!=null&&actual!=null&&MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),actual.getBytes(StandardCharsets.UTF_8));}

    private PlatformServiceException unauthorized(String message) {
        return new PlatformServiceException(
                PlatformErrorCode.ACCESS_DENIED,
                HttpStatus.UNAUTHORIZED,
                message,
                "اعتبارنامه فراخوانی اتوماسیون نامعتبر است.",
                Map.of(),
                null
        );
    }
}
