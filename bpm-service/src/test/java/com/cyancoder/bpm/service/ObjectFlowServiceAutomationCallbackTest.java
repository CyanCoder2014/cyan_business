package com.cyancoder.bpm.service;

import com.cyancoder.bpm.api.dto.AsyncActionCallbackRequest;
import com.cyancoder.bpm.api.dto.BpmScope;
import com.cyancoder.bpm.api.dto.TransitionActorContext;
import com.cyancoder.bpm.domain.AutomationBlockExecution;
import com.cyancoder.bpm.domain.DynamicFlowDefinition;
import com.cyancoder.bpm.domain.FlowState;
import com.cyancoder.bpm.domain.FlowTransition;
import com.cyancoder.bpm.domain.ManagedObject;
import com.cyancoder.bpm.domain.ManagedObjectRef;
import com.cyancoder.bpm.domain.ActionType;
import com.cyancoder.bpm.domain.AutomationExecutionMode;
import com.cyancoder.bpm.domain.AutomationFailurePolicy;
import com.cyancoder.bpm.domain.SubmitMode;
import com.cyancoder.bpm.repo.ManagedObjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ObjectFlowServiceAutomationCallbackTest {

    @Test
    void callbackMapsAutomationOutputAndTransitionsAutomaticState() {
        ManagedObjectRepository repository = mock(ManagedObjectRepository.class);
        FlowDefinitionService flowDefinitionService = mock(FlowDefinitionService.class);
        DynamicFlowIntegrationClient integrationClient = mock(DynamicFlowIntegrationClient.class);
        FlowActionExecutor flowActionExecutor = mock(FlowActionExecutor.class);
        ObjectProvider<org.flowable.engine.RuntimeService> runtimeProvider = mock(ObjectProvider.class);
        ObjectProvider<org.flowable.engine.TaskService> taskProvider = mock(ObjectProvider.class);
        when(runtimeProvider.getIfAvailable()).thenReturn(null);
        when(taskProvider.getIfAvailable()).thenReturn(null);
        doAnswer(invocation -> invocation.getArgument(0)).when(repository).save(any(ManagedObject.class));

        ObjectFlowService service = new ObjectFlowService(
                repository,
                flowDefinitionService,
                new FlowTransitionConditionEvaluator(),
                integrationClient,
                flowActionExecutor,
                runtimeProvider,
                taskProvider,
                mock(BpmAssignmentDirectoryService.class)
        );

        ManagedObject object = new ManagedObject();
        object.setId("obj-1");
        object.setTenantKey("tenant-demo");
        object.setSiteKey("site-demo");
        object.setFlowKey("hybrid-screening-intake");
        object.setState("automated-screening");
        object.setObjectType("SCREENING");
        ManagedObjectRef ref = new ManagedObjectRef();
        ref.setService("bpm-service");
        ref.setEntityKey("screening");
        ref.setRecordKey("obj-1");
        object.setObjectRef(ref);
        object.setPayload(new LinkedHashMap<>());
        AutomationBlockExecution block = new AutomationBlockExecution();
        block.setBlockKey("screening");
        block.setAutomationFlowKey("hybrid-screening-automation");
        block.setExecutionMode(AutomationExecutionMode.ASYNC);
        block.setFailurePolicy(AutomationFailurePolicy.MARK_FAILED);
        block.setCorrelationKey("obj-1:screening");
        block.setStateId("automated-screening");
        block.setStatus("PENDING");
        block.setWaitForCompletion(true);
        block.setOutputMappings(new LinkedHashMap<>(Map.of(
                "payload.currentFormValues.riskScore", "riskScore",
                "payload.currentFormValues.screeningRoute", "screeningRoute",
                "payload.currentFormValues.externalRef", "externalRef"
        )));
        block.setStoreOutputAt("payload.automation.screening.snapshot");
        object.setAutomationBlockRegistry(new java.util.ArrayList<>(List.of(block)));

        DynamicFlowDefinition definition = new DynamicFlowDefinition();
        definition.setFlowKey("hybrid-screening-intake");
        definition.setStates(List.of(
                new FlowState("automated-screening", "Automated Screening", false, null, null, false, Set.of(), List.of(), null, null, null, null, null, SubmitMode.DYNAMIC, null, true),
                new FlowState("fast-track-approved", "Fast Track", true, null, null, false, Set.of(), List.of(), null, null, null, null, null, SubmitMode.DYNAMIC, null, false)
        ));
        definition.setTransitions(List.of(
                new FlowTransition("fast-track", "automated-screening", "fast-track-approved", "Fast Track", Set.of(), Set.of(), "payload.currentFormValues.screeningRoute == \"FAST_TRACK\"", null, List.of())
        ));

        when(repository.findById("obj-1")).thenReturn(Optional.of(object));
        when(flowDefinitionService.getActiveByFlowKey(new BpmScope("tenant-demo", "site-demo"), "hybrid-screening-intake")).thenReturn(definition);

        ManagedObject result = service.acceptAsyncActionCallback(
                new BpmScope("tenant-demo", "site-demo"),
                "obj-1",
                "screening",
                new AsyncActionCallbackRequest("cb-1", "SUCCESS", null, Map.of(
                        "riskScore", 22,
                        "screeningRoute", "FAST_TRACK",
                        "externalRef", "screen-abcd",
                        "snapshot", Map.of("providerDecision", "FAST_TRACK")
                ), Map.of()),
                new TransitionActorContext("system-callback", Set.of("creator"), Set.of("FLOW_SUBMIT")),
                "callbackId:cb-1"
        );

        assertEquals("fast-track-approved", result.getState());
        assertEquals(22, ActionPayloadSupport.readPath(result.getPayload(), "currentFormValues.riskScore"));
        assertEquals("FAST_TRACK", ActionPayloadSupport.readPath(result.getPayload(), "currentFormValues.screeningRoute"));
        assertEquals("screen-abcd", ActionPayloadSupport.readPath(result.getPayload(), "currentFormValues.externalRef"));
        assertEquals("FAST_TRACK", ActionPayloadSupport.readPath(result.getPayload(), "automation.screening.snapshot.snapshot.providerDecision"));
        assertEquals("SUCCESS", result.getAutomationBlockRegistry().get(0).getStatus());
    }

    @Test
    void callbackFailureWithRetryPolicyRequeuesAutomationBlock() {
        ManagedObjectRepository repository = mock(ManagedObjectRepository.class);
        FlowDefinitionService flowDefinitionService = mock(FlowDefinitionService.class);
        DynamicFlowIntegrationClient integrationClient = mock(DynamicFlowIntegrationClient.class);
        FlowActionExecutor flowActionExecutor = mock(FlowActionExecutor.class);
        ObjectProvider<org.flowable.engine.RuntimeService> runtimeProvider = mock(ObjectProvider.class);
        ObjectProvider<org.flowable.engine.TaskService> taskProvider = mock(ObjectProvider.class);
        when(runtimeProvider.getIfAvailable()).thenReturn(null);
        when(taskProvider.getIfAvailable()).thenReturn(null);
        doAnswer(invocation -> invocation.getArgument(0)).when(repository).save(any(ManagedObject.class));

        ObjectFlowService service = new ObjectFlowService(
                repository,
                flowDefinitionService,
                new FlowTransitionConditionEvaluator(),
                integrationClient,
                flowActionExecutor,
                runtimeProvider,
                taskProvider,
                mock(BpmAssignmentDirectoryService.class)
        );

        ManagedObject object = new ManagedObject();
        object.setId("obj-2");
        object.setTenantKey("tenant-demo");
        object.setSiteKey("site-demo");
        object.setFlowKey("hybrid-screening-intake");
        object.setState("automated-screening");
        object.setPayload(new LinkedHashMap<>());

        AutomationBlockExecution block = new AutomationBlockExecution();
        block.setBlockKey("screening");
        block.setAutomationFlowKey("hybrid-screening-automation");
        block.setExecutionMode(AutomationExecutionMode.ASYNC);
        block.setFailurePolicy(AutomationFailurePolicy.RETRY);
        block.setCorrelationKey("obj-2:screening");
        block.setStateId("automated-screening");
        block.setStatus("RUNNING");
        block.setWaitForCompletion(true);
        block.setMaxRetries(1);
        block.setRetryCount(0);
        block.setServiceKey("automation-orchestrator-service");
        block.setPath("/internal/automation-orchestrator/executions/start");
        block.setMethod("POST");
        block.setRequestBody(new LinkedHashMap<>(Map.of("requestedAmount", 15000)));
        object.setAutomationBlockRegistry(new java.util.ArrayList<>(List.of(block)));

        when(repository.findById("obj-2")).thenReturn(Optional.of(object));

        service.acceptAsyncActionCallback(
                new BpmScope("tenant-demo", "site-demo"),
                "obj-2",
                "screening",
                new AsyncActionCallbackRequest("cb-2", "FAILED", null, Map.of("error", "provider down"), Map.of()),
                new TransitionActorContext("system-callback", Set.of("creator"), Set.of("FLOW_SUBMIT")),
                "callbackId:cb-2"
        );

        verify(flowActionExecutor).execute(argThat(actions ->
                        actions.size() == 1 && actions.get(0).type() == ActionType.RUN_AUTOMATION_BLOCK),
                any(ManagedObject.class),
                any(BpmScope.class),
                any(String.class));
        assertEquals(1, object.getAutomationBlockRegistry().get(0).getRetryCount());
    }
}
