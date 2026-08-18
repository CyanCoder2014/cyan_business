package com.cyancoder.bpm.service;

import com.cyancoder.bpm.api.dto.BpmScope;
import com.cyancoder.bpm.api.dto.CreateManagedObjectRequest;
import com.cyancoder.bpm.api.dto.TransitionActorContext;
import com.cyancoder.bpm.domain.DynamicFlowDefinition;
import com.cyancoder.bpm.domain.FlowState;
import com.cyancoder.bpm.domain.FlowTransition;
import com.cyancoder.bpm.domain.ManagedObject;
import com.cyancoder.bpm.domain.ManagedObjectRef;
import com.cyancoder.bpm.domain.SubmitMode;
import com.cyancoder.bpm.repo.ManagedObjectRepository;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ObjectFlowServiceCompletionTest {

    @Test
    void marksAnObjectCompletedAfterAutomaticTransitionToTerminalState() {
        ManagedObjectRepository repository = mock(ManagedObjectRepository.class);
        FlowDefinitionService definitions = mock(FlowDefinitionService.class);
        ObjectProvider<RuntimeService> runtimeProvider = mock(ObjectProvider.class);
        ObjectProvider<TaskService> taskProvider = mock(ObjectProvider.class);
        when(runtimeProvider.getIfAvailable()).thenReturn(null);
        when(taskProvider.getIfAvailable()).thenReturn(null);
        doAnswer(invocation -> {
            ManagedObject object = invocation.getArgument(0);
            if (object.getId() == null) object.setId("work-1");
            return object;
        }).when(repository).save(any(ManagedObject.class));

        DynamicFlowDefinition definition = new DynamicFlowDefinition();
        definition.setFlowKey("completion-check");
        definition.setStartState("route");
        definition.setStates(List.of(
                state("route", false),
                state("completed", true)
        ));
        definition.setTransitions(List.of(
                new FlowTransition("finish", "route", "completed", "Finish", Set.of(), Set.of(), null, null, List.of())
        ));
        BpmScope scope = new BpmScope("tenant", "site");
        when(definitions.getActiveByFlowKey(scope, "completion-check")).thenReturn(definition);

        ObjectFlowService service = new ObjectFlowService(
                repository,
                definitions,
                new FlowTransitionConditionEvaluator(),
                mock(DynamicFlowIntegrationClient.class),
                mock(FlowActionExecutor.class),
                runtimeProvider,
                taskProvider,
                mock(BpmAssignmentDirectoryService.class)
        );
        ManagedObjectRef reference = new ManagedObjectRef();
        reference.setService("bpm-service");
        reference.setEntityKey("completion-check");
        reference.setRecordKey("record-1");

        ManagedObject result = service.createAndStart(
                scope,
                new CreateManagedObjectRequest("completion-check", "TEST", reference, Map.of()),
                new TransitionActorContext("tester", Set.of(), Set.of())
        );

        assertThat(result.getState()).isEqualTo("completed");
        assertThat(result.getCompletedAt()).isNotNull();
    }

    private static FlowState state(String id, boolean terminal) {
        return new FlowState(id, id, terminal, null, null, false, Set.of(), List.of(), null,
                null, null, null, null, SubmitMode.STATIC, null, false);
    }
}
