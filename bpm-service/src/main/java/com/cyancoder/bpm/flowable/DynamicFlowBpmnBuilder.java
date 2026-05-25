package com.cyancoder.bpm.flowable;

import com.cyancoder.bpm.domain.DynamicFlowDefinition;
import com.cyancoder.bpm.domain.FlowState;
import com.cyancoder.bpm.domain.FlowTransition;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.UserTask;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class DynamicFlowBpmnBuilder {

    public BpmnModel build(DynamicFlowDefinition definition) {
        if (definition.getFlowKey() == null || definition.getFlowKey().isBlank()) {
            throw new IllegalArgumentException("flowKey is required");
        }
        if (definition.getStates() == null || definition.getStates().isEmpty()) {
            throw new IllegalArgumentException("states are required");
        }
        if (definition.getStartState() == null || definition.getStartState().isBlank()) {
            throw new IllegalArgumentException("startState is required");
        }

        Process process = new Process();
        process.setId(definition.getFlowKey());
        process.setName(Objects.requireNonNullElse(definition.getName(), definition.getFlowKey()));

        StartEvent startEvent = new StartEvent();
        startEvent.setId("start");
        process.addFlowElement(startEvent);

        Map<String, UserTask> stateTasks = new HashMap<>();
        for (FlowState state : definition.getStates()) {
            UserTask task = new UserTask();
            task.setId(taskId(state.id()));
            task.setName(Objects.requireNonNullElse(state.displayName(), state.id()));
            if (state.candidateGroups() != null && !state.candidateGroups().isEmpty()) {
                task.setCandidateGroups(state.candidateGroups().stream().toList());
            }
            process.addFlowElement(task);
            stateTasks.put(state.id(), task);
        }

        SequenceFlow startToFirstState = new SequenceFlow();
        startToFirstState.setId("flow_start");
        startToFirstState.setSourceRef("start");
        startToFirstState.setTargetRef(taskId(definition.getStartState()));
        process.addFlowElement(startToFirstState);

        if (definition.getTransitions() != null) {
            for (FlowTransition transition : definition.getTransitions()) {
                SequenceFlow sequenceFlow = new SequenceFlow();
                sequenceFlow.setId(flowId(transition.id(), transition.fromState(), transition.toState()));
                sequenceFlow.setSourceRef(taskId(transition.fromState()));
                sequenceFlow.setTargetRef(taskId(transition.toState()));
                sequenceFlow.setName(transition.label());
                sequenceFlow.setConditionExpression("${nextState == '" + transition.toState() + "'}");
                process.addFlowElement(sequenceFlow);
            }
        }

        for (FlowState state : definition.getStates()) {
            if (!state.terminal()) {
                continue;
            }
            EndEvent endEvent = new EndEvent();
            endEvent.setId("end_" + state.id());
            process.addFlowElement(endEvent);

            SequenceFlow toEnd = new SequenceFlow();
            toEnd.setId("flow_" + state.id() + "_to_end");
            toEnd.setSourceRef(taskId(state.id()));
            toEnd.setTargetRef(endEvent.getId());
            toEnd.setConditionExpression("${nextState == '" + state.id() + "'}");
            process.addFlowElement(toEnd);
        }

        BpmnModel model = new BpmnModel();
        model.addProcess(process);

        process.findFlowElementsOfType(SequenceFlow.class).forEach(sequenceFlow -> {
            process.findFlowElementsOfType(UserTask.class).forEach(task -> {
                if (task.getId().equals(sequenceFlow.getSourceRef())) {
                    if (task.getOutgoingFlows() == null) {
                        task.setOutgoingFlows(new ArrayList<>());
                    }
                    task.getOutgoingFlows().add(sequenceFlow);
                }
                if (task.getId().equals(sequenceFlow.getTargetRef())) {
                    if (task.getIncomingFlows() == null) {
                        task.setIncomingFlows(new ArrayList<>());
                    }
                    task.getIncomingFlows().add(sequenceFlow);
                }
            });
            if (startEvent.getId().equals(sequenceFlow.getSourceRef())) {
                if (startEvent.getOutgoingFlows() == null) {
                    startEvent.setOutgoingFlows(new ArrayList<>());
                }
                startEvent.getOutgoingFlows().add(sequenceFlow);
            }
        });

        return model;
    }

    private String taskId(String stateId) {
        return "state_" + stateId;
    }

    private String flowId(String transitionId, String fromState, String toState) {
        if (transitionId != null && !transitionId.isBlank()) {
            return "flow_" + transitionId;
        }
        return "flow_" + fromState + "_to_" + toState;
    }
}

