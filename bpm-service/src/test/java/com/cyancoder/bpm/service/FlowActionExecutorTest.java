package com.cyancoder.bpm.service;

import com.cyancoder.bpm.api.dto.BpmScope;
import com.cyancoder.bpm.domain.ActionType;
import com.cyancoder.bpm.domain.FlowActionConfig;
import com.cyancoder.bpm.domain.ManagedObject;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FlowActionExecutorTest {

    @Test
    void runAutomationBlockStoresFirstClassBlockAndInitialResponse() {
        DynamicFlowIntegrationClient integrationClient = mock(DynamicFlowIntegrationClient.class);
        FlowActionExecutor executor = new FlowActionExecutor(integrationClient);

        when(integrationClient.callActionForResponse(any(), any(), any(), any())).thenReturn(Map.of(
                "executionId", "exec-123",
                "status", "RUNNING",
                "snapshot", Map.of("stage", "accepted")
        ));

        ManagedObject object = new ManagedObject();
        object.setId("obj-1");
        object.setState("automated-screening");
        object.setFlowKey("hybrid-screening-intake");
        object.setPayload(new LinkedHashMap<>(Map.of(
                "screening-intake", Map.of(
                        "fullName", "Jane Roe",
                        "nationalId", "99887766",
                        "requestedAmount", 15000
                )
        )));

        executor.execute(List.of(new FlowActionConfig(ActionType.RUN_AUTOMATION_BLOCK, Map.of(
                "blockKey", "screening",
                "automationFlowKey", "hybrid-screening-automation",
                "executionMode", "ASYNC",
                "body", Map.of(
                        "fullName", "{{payload.screening-intake.fullName}}",
                        "requestedAmount", "{{payload.screening-intake.requestedAmount}}"
                ),
                "responseMappings", Map.of(
                        "payload.automation.screening.executionId", "executionId",
                        "payload.automation.screening.status", "status"
                ),
                "storeFullResponseAt", "payload.automation.screening.startResponse"
        ))), object, new BpmScope("tenant-demo", "site-demo"), "user-1");

        assertEquals("exec-123", ActionPayloadSupport.readPath(object.getPayload(), "automation.screening.executionId"));
        assertEquals("RUNNING", ActionPayloadSupport.readPath(object.getPayload(), "automation.screening.status"));
        assertEquals("accepted", ActionPayloadSupport.readPath(object.getPayload(), "automation.screening.startResponse.snapshot.stage"));
        assertEquals("screening", object.getAutomationBlockRegistry().get(0).getBlockKey());
        assertEquals("RUNNING", object.getAutomationBlockRegistry().get(0).getStatus());
        assertEquals("obj-1:screening", object.getAutomationBlockRegistry().get(0).getCorrelationKey());
    }

    @Test
    void syncAutomationBlockWithFailFastThrowsOnFailure() {
        DynamicFlowIntegrationClient integrationClient = mock(DynamicFlowIntegrationClient.class);
        FlowActionExecutor executor = new FlowActionExecutor(integrationClient);

        when(integrationClient.callActionForResponse(any(), any(), any(), any())).thenReturn(Map.of(
                "executionId", "exec-fail",
                "status", "FAILED",
                "error", Map.of("message", "downstream failure")
        ));

        ManagedObject object = new ManagedObject();
        object.setId("obj-2");
        object.setState("automated-screening");
        object.setFlowKey("hybrid-screening-intake");
        object.setPayload(new LinkedHashMap<>());

        assertThrows(IllegalStateException.class, () -> executor.execute(List.of(
                new FlowActionConfig(ActionType.RUN_AUTOMATION_BLOCK, Map.of(
                        "blockKey", "screening",
                        "automationFlowKey", "hybrid-screening-automation",
                        "executionMode", "SYNC",
                        "failurePolicy", "FAIL_FAST",
                        "body", Map.of()
                ))
        ), object, new BpmScope("tenant-demo", "site-demo"), "user-1"));
    }

    @Test
    void runAutomationBlockAcceptsGeneratedNaviyaAliases() {
        DynamicFlowIntegrationClient integrationClient = mock(DynamicFlowIntegrationClient.class);
        FlowActionExecutor executor = new FlowActionExecutor(integrationClient);

        when(integrationClient.callActionForResponse(any(), any(), any(), any())).thenReturn(Map.of(
                "executionId", "exec-456",
                "status", "COMPLETED",
                "output", Map.of("screeningRoute", "FAST_TRACK", "riskScore", 21)
        ));

        ManagedObject object = new ManagedObject();
        object.setId("obj-3");
        object.setState("automation-screening");
        object.setFlowKey("ai-assisted-screening-review");
        object.setPayload(new LinkedHashMap<>(Map.of(
                "intake", Map.of(
                        "fullName", "Jane Roe",
                        "nationalId", "99887766",
                        "requestedAmount", 15000
                )
        )));

        executor.execute(List.of(new FlowActionConfig(ActionType.RUN_AUTOMATION_BLOCK, Map.of(
                "actionKey", "screening",
                "flowKey", "hybrid-screening-automation",
                "async", false,
                "variables", Map.of(
                        "fullName", "{{payload.intake.fullName}}",
                        "requestedAmount", "{{payload.intake.requestedAmount}}"
                ),
                "storeExecutionIdAt", "payload.automation.screening.executionId",
                "storeStatusAt", "payload.automation.screening.status",
                "storeVariablesAt", "payload.automation.screening.output",
                "resultMappings", Map.of(
                        "payload.currentFormValues.screeningRoute", "screeningRoute"
                )
        ))), object, new BpmScope("tenant-demo", "site-demo"), "user-1");

        assertEquals("exec-456", ActionPayloadSupport.readPath(object.getPayload(), "automation.screening.executionId"));
        assertEquals("COMPLETED", ActionPayloadSupport.readPath(object.getPayload(), "automation.screening.status"));
        assertEquals("FAST_TRACK", ActionPayloadSupport.readPath(object.getPayload(), "automation.screening.output.screeningRoute"));
        assertEquals("FAST_TRACK", ActionPayloadSupport.readPath(object.getPayload(), "currentFormValues.screeningRoute"));
        assertEquals("hybrid-screening-automation", object.getAutomationBlockRegistry().get(0).getAutomationFlowKey());
    }
}
