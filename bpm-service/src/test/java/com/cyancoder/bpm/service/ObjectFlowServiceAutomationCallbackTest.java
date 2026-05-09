package com.cyancoder.bpm.service;

import com.cyancoder.bpm.api.dto.AsyncActionCallbackRequest;
import com.cyancoder.bpm.api.dto.BpmScope;
import com.cyancoder.bpm.api.dto.TransitionActorContext;
import com.cyancoder.bpm.domain.AsyncActionRegistration;
import com.cyancoder.bpm.domain.DynamicFlowDefinition;
import com.cyancoder.bpm.domain.FlowState;
import com.cyancoder.bpm.domain.FlowTransition;
import com.cyancoder.bpm.domain.ManagedObject;
import com.cyancoder.bpm.domain.ManagedObjectRef;
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
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
                taskProvider
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
        Map<String, Object> screeningEntry = new LinkedHashMap<>();
        screeningEntry.put("status", "PENDING");
        screeningEntry.put("callbackResponseMappings", Map.of(
                "payload.currentFormValues.riskScore", "riskScore",
                "payload.currentFormValues.screeningRoute", "screeningRoute",
                "payload.currentFormValues.externalRef", "externalRef"
        ));
        screeningEntry.put("callbackStoreFullResponseAt", "payload.automation.screening.snapshot");
        Map<String, Object> asyncActions = new LinkedHashMap<>();
        asyncActions.put("screening", screeningEntry);
        object.setPayload(new LinkedHashMap<>(Map.of("asyncActions", asyncActions)));
        AsyncActionRegistration registration = new AsyncActionRegistration();
        registration.setActionKey("screening");
        registration.setCorrelationKey("obj-1:screening");
        registration.setStateId("automated-screening");
        registration.setStatus("PENDING");
        object.setAsyncActionRegistry(new java.util.ArrayList<>(List.of(registration)));

        DynamicFlowDefinition definition = new DynamicFlowDefinition();
        definition.setFlowKey("hybrid-screening-intake");
        definition.setStates(List.of(
                new FlowState("automated-screening", "Automated Screening", false, null, null, false, Set.of(), List.of(), null, null, null, null, null, SubmitMode.DYNAMIC, null),
                new FlowState("fast-track-approved", "Fast Track", true, null, null, false, Set.of(), List.of(), null, null, null, null, null, SubmitMode.DYNAMIC, null)
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
        assertEquals("SUCCESS", result.getAsyncActionRegistry().get(0).getStatus());
    }
}
