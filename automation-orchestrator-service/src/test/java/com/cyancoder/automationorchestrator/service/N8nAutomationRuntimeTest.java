package com.cyancoder.automationorchestrator.service;

import com.cyancoder.automationorchestrator.domain.*;
import com.cyancoder.automationorchestrator.repo.AutomationExecutionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class N8nAutomationRuntimeTest {
    private final InternalServiceHttpSupport http = mock(InternalServiceHttpSupport.class);
    private final N8nAutomationRuntime runtime = new N8nAutomationRuntime(
            http, mock(ConnectorCredentialService.class), mock(AutomationFlowDefinitionService.class),
            mock(AutomationExecutionRepository.class), new ObjectMapper().findAndRegisterModules(),
            new N8nExpressionService(), ""
    );

    @Test
    void executesItemStreamBranchMergeTransformAndAggregation() {
        AutomationFlowDefinition definition = definition(List.of(
                node("trigger", AutomationNodeType.MANUAL_TRIGGER, Map.of()),
                node("split", AutomationNodeType.SPLIT_OUT, Map.of("field", "values", "targetField", "value")),
                node("edit", AutomationNodeType.EDIT_FIELDS, Map.of("assignments", Map.of("doubled", "={{$json.value * 2}}"))),
                node("if", AutomationNodeType.IF, Map.of("field", "={{$json.doubled}}", "operator", "GTE", "value", 4)),
                node("merge", AutomationNodeType.MERGE, Map.of("mode", "APPEND")),
                node("sort", AutomationNodeType.SORT, Map.of("field", "doubled", "direction", "ASC")),
                node("aggregate", AutomationNodeType.AGGREGATE, Map.of("field", "doubled", "targetField", "values")),
                node("end", AutomationNodeType.END, Map.of())
        ), List.of(
                edge("trigger", "0", "split", "0"), edge("split", "0", "edit", "0"),
                edge("edit", "0", "if", "0"), edge("if", "true", "merge", "0"),
                edge("if", "false", "merge", "1"), edge("merge", "0", "sort", "0"),
                edge("sort", "0", "aggregate", "0"), edge("aggregate", "0", "end", "0")
        ));
        AutomationExecution execution = execution(Map.of("values", List.of(1, 2, 3)));

        runtime.run(execution, definition);

        assertEquals("COMPLETED", execution.getStatus());
        assertEquals(List.of(2.0, 4.0, 6.0), AutomationDataSupport.readPath(execution.getOutput(), "json.values"));
        assertEquals(2, execution.getSteps().stream().filter(step -> "MERGE".equals(step.getNodeType())).count());
    }

    @Test
    void retriesHttpNodeAndPreservesItemShape() {
        when(http.exchangeUrl(eq("https://example.test"), eq(HttpMethod.GET), any(), any(), any(), any(), eq(Object.class)))
                .thenThrow(new IllegalStateException("temporary"))
                .thenReturn(Map.of("ok", true));
        AutomationRetryPolicy retry = new AutomationRetryPolicy(2, 0L, "fixed");
        AutomationNode request = new AutomationNode("request", AutomationNodeType.HTTP_REQUEST, "request", true,
                null, retry, null, null, null, Map.of("url", "https://example.test", "responsePath", "api"), null, null);
        AutomationFlowDefinition definition = definition(List.of(
                node("trigger", AutomationNodeType.MANUAL_TRIGGER, Map.of()), request, node("end", AutomationNodeType.END, Map.of())
        ), List.of(edge("trigger", "0", "request", "0"), edge("request", "0", "end", "0")));
        AutomationExecution execution = execution(Map.of("id", "a"));

        runtime.run(execution, definition);

        assertEquals(true, AutomationDataSupport.readPath(execution.getOutput(), "json.api.ok"));
        assertEquals(List.of("RETRYING", "COMPLETED"), execution.getSteps().stream()
                .filter(step -> "request".equals(step.getNodeId())).map(AutomationExecutionStep::getStatus).toList());
    }

    @Test
    void loopOverItemsWaitsForFeedbackAndEmitsProcessedItemsOnDone() {
        AutomationFlowDefinition definition = definition(List.of(
                node("trigger", AutomationNodeType.MANUAL_TRIGGER, Map.of()),
                node("loop", AutomationNodeType.LOOP_OVER_ITEMS, Map.of("batchSize", 2)),
                node("edit", AutomationNodeType.EDIT_FIELDS, Map.of("assignments", Map.of("visited", true))),
                node("end", AutomationNodeType.END, Map.of())
        ), List.of(
                edge("trigger", "0", "loop", "0"),
                edge("loop", "loop", "edit", "0"),
                edge("edit", "0", "loop", "0"),
                edge("loop", "done", "end", "0")
        ));
        AutomationExecution execution = execution(Map.of("items", List.of(
                Map.of("id", 1), Map.of("id", 2), Map.of("id", 3)
        )));

        runtime.run(execution, definition);

        assertEquals("COMPLETED", execution.getStatus());
        assertEquals(3, AutomationDataSupport.list(execution.getOutput().get("items")).size());
        assertEquals(true, AutomationDataSupport.readPath(execution.getOutput(), "items.0.json.visited"));
        assertEquals(3, execution.getSteps().stream().filter(step -> "loop".equals(step.getNodeId())).count());
    }

    @Test
    void persistsCallbackWaitAndResumesIdempotently() {
        AutomationFlowDefinition definition = definition(List.of(
                node("trigger", AutomationNodeType.WEBHOOK_TRIGGER, Map.of()),
                node("wait", AutomationNodeType.WAIT_FOR_CALLBACK, Map.of(
                        "callbackMappings", Map.of("review.decision", "decision"))),
                node("end", AutomationNodeType.END, Map.of())
        ), List.of(edge("trigger", "0", "wait", "0"), edge("wait", "callback", "end", "0")));
        AutomationExecution execution = execution(Map.of("caseId", "c-1"));

        runtime.run(execution, definition);
        assertEquals("WAITING_CALLBACK", execution.getStatus());
        runtime.callback(execution, definition, "wait", "cb-1", Map.of("decision", "APPROVE"));
        int steps = execution.getSteps().size();

        assertEquals("COMPLETED", execution.getStatus());
        assertEquals("APPROVE", AutomationDataSupport.readPath(execution.getOutput(), "json.review.decision"));
        runtime.callback(execution, definition, "wait", "cb-1", Map.of("decision", "REJECT"));
        assertEquals(steps, execution.getSteps().size());
    }

    private AutomationExecution execution(Map<String, Object> input) {
        AutomationExecution execution = new AutomationExecution();
        execution.setExecutionId("exec-test");
        execution.setTenantKey("tenant");
        execution.setSiteKey("site");
        execution.setStatus("RUNNING");
        execution.setInput(new LinkedHashMap<>(input));
        execution.setOutput(new LinkedHashMap<>());
        execution.setContext(new LinkedHashMap<>());
        return execution;
    }

    private AutomationFlowDefinition definition(List<AutomationNode> nodes, List<AutomationEdge> edges) {
        AutomationFlowDefinition definition = new AutomationFlowDefinition();
        definition.setFlowKey("item-flow");
        definition.setName("Item Flow");
        definition.setRuntimeMode("N8N_ITEMS");
        definition.setEntryNodeId("trigger");
        definition.setNodes(nodes);
        definition.setEdges(edges);
        return definition;
    }

    private AutomationNode node(String id, AutomationNodeType type, Map<String, Object> config) {
        return new AutomationNode(id, type, id, true, null, null, null, null, null, config, null, null);
    }

    private AutomationEdge edge(String from, String fromPort, String to, String toPort) {
        return new AutomationEdge(from + "-" + to + "-" + fromPort, from, fromPort, to, toPort);
    }
}
