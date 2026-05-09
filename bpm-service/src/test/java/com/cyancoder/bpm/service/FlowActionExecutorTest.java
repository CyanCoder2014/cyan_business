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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FlowActionExecutorTest {

    @Test
    void startAutomationFlowStoresRegistrationAndInitialResponse() {
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

        executor.execute(List.of(new FlowActionConfig(ActionType.START_AUTOMATION_FLOW, Map.of(
                "actionKey", "screening",
                "automationFlowKey", "hybrid-screening-automation",
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

        assertEquals("PENDING", ActionPayloadSupport.readPath(object.getPayload(), "asyncActions.screening.status"));
        assertEquals("START_AUTOMATION_FLOW", ActionPayloadSupport.readPath(object.getPayload(), "asyncActions.screening.actionType"));
        assertEquals("exec-123", ActionPayloadSupport.readPath(object.getPayload(), "automation.screening.executionId"));
        assertEquals("RUNNING", ActionPayloadSupport.readPath(object.getPayload(), "automation.screening.status"));
        assertEquals("accepted", ActionPayloadSupport.readPath(object.getPayload(), "automation.screening.startResponse.snapshot.stage"));
        assertEquals("screening", object.getAsyncActionRegistry().get(0).getActionKey());
    }
}
