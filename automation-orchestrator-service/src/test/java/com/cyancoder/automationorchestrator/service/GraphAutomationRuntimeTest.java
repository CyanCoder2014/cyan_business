package com.cyancoder.automationorchestrator.service;

import com.cyancoder.automationorchestrator.domain.*;
import com.cyancoder.automationorchestrator.repo.AutomationExecutionRepository;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GraphAutomationRuntimeTest {
    private final InternalServiceHttpSupport http = mock(InternalServiceHttpSupport.class);
    private final ConnectorCredentialService credentials = mock(ConnectorCredentialService.class);
    private final AutomationFlowDefinitionService flows = mock(AutomationFlowDefinitionService.class);
    private final AutomationExecutionRepository executions = mock(AutomationExecutionRepository.class);
    private final GoRulesDecisionService decisions = mock(GoRulesDecisionService.class);
    private final GraphAutomationRuntime runtime = new GraphAutomationRuntime(http, credentials, flows, executions, decisions);

    @Test
    void executesTransformIterationDedupCodeBranchAndFileNodes() {
        AutomationExecution execution = execution(Map.of(
                "amount", 4,
                "items", List.of(Map.of("id", "a", "value", 1), Map.of("id", "a", "value", 2), Map.of("id", "b", "value", 3)),
                "file", Map.of("base64", Base64.getEncoder().encodeToString("hello".getBytes()))
        ));
        List<AutomationNode> nodes = List.of(
                node("trigger", AutomationNodeType.WEBHOOK_TRIGGER, Map.of()),
                node("map", AutomationNodeType.MAP_FIELDS, Map.of("mappings", Map.of("result.amount", "amount"))),
                node("each", AutomationNodeType.FOR_EACH, Map.of("sourcePath", "items", "itemTemplate", Map.of("id", "{{item.id}}", "value", "{{item.value}}"), "targetPath", "processed")),
                node("dedup", AutomationNodeType.DEDUP_BY_KEY, Map.of("sourcePath", "processed", "keyPath", "id", "targetPath", "unique")),
                node("code", AutomationNodeType.CODE, Map.of("expression", "#variables['amount'] * 2", "targetPath", "score")),
                node("check", AutomationNodeType.IF, Map.of("field", "score", "operator", "GTE", "value", 8)),
                node("json", AutomationNodeType.JSON_TRANSFORM, Map.of("template", Map.of("accepted", true, "score", "{{score}}"), "targetPath", "decision")),
                node("file", AutomationNodeType.FILE_METADATA, Map.of("sourcePath", "file", "targetPath", "fileMeta")),
                node("end", AutomationNodeType.END, Map.of())
        );
        AutomationFlowDefinition definition = definition(nodes, List.of(
                edge("trigger", null, "map"), edge("map", null, "each"), edge("each", null, "dedup"),
                edge("dedup", null, "code"), edge("code", null, "check"), edge("check", "true", "json"),
                edge("check", "false", "end"), edge("json", null, "file"), edge("file", null, "end")
        ));

        runtime.run(execution, definition);

        assertEquals("COMPLETED", execution.getStatus());
        assertEquals(8, execution.getOutput().get("score"));
        assertEquals(2, AutomationDataSupport.list(execution.getOutput().get("unique")).size());
        assertEquals(5, AutomationDataSupport.map(execution.getOutput().get("fileMeta")).get("size"));
        assertTrue(execution.getSteps().stream().allMatch(step -> "COMPLETED".equals(step.getStatus())));
    }

    @Test
    void pausesForCallbackThenMapsPayloadAndResumes() {
        AutomationExecution execution = execution(Map.of("caseId", "c-1"));
        AutomationFlowDefinition definition = definition(List.of(
                node("trigger", AutomationNodeType.WEBHOOK_TRIGGER, Map.of()),
                node("wait", AutomationNodeType.WAIT_FOR_CALLBACK, Map.of("callbackMappings", Map.of("review.decision", "decision"), "callbackStorePath", "review.raw")),
                node("end", AutomationNodeType.END, Map.of())
        ), List.of(edge("trigger", null, "wait"), edge("wait", "callback", "end")));

        runtime.run(execution, definition);
        assertEquals("WAITING_CALLBACK", execution.getStatus());

        runtime.callback(execution, definition, "wait", "cb-1", Map.of("decision", "APPROVE"));

        assertEquals("COMPLETED", execution.getStatus());
        assertEquals("APPROVE", AutomationDataSupport.readPath(execution.getOutput(), "review.decision"));
        int stepCount = execution.getSteps().size();
        runtime.callback(execution, definition, "wait", "cb-1", Map.of("decision", "REJECT"));
        assertEquals(stepCount, execution.getSteps().size());
    }

    @Test
    void executesPaginationJdmAndN8nDelegation() {
        AutomationExecution execution = execution(Map.of());
        when(http.exchangeUrl(eq("https://api.test/pages"), any(), any(), any(), any(), any(), eq(Object.class)))
                .thenReturn(Map.of("content", List.of(Map.of("id", "a"))))
                .thenReturn(Map.of("content", List.of(Map.of("id", "b"))));
        when(http.exchangeUrl(eq("https://n8n.test/webhook/enrich"), any(), any(), any(), any(), any(), eq(Object.class)))
                .thenReturn(Map.of("provider", "n8n"));
        when(decisions.evaluate(anyMap(), anyMap())).thenReturn(Map.of("result", Map.of("route", "APPROVE")));
        AutomationFlowDefinition definition = definition(List.of(
                node("trigger", AutomationNodeType.WEBHOOK_TRIGGER, Map.of()),
                node("pages", AutomationNodeType.PAGINATED_CALL_API, Map.of("url", "https://api.test/pages", "pageCount", 2, "itemsPath", "content", "targetPath", "rows")),
                node("jdm", AutomationNodeType.JDM_DECISION, Map.of("jdm", Map.of(), "outputPath", "decision")),
                node("n8n", AutomationNodeType.N8N_WORKFLOW, Map.of("webhookUrl", "https://n8n.test/webhook/enrich", "storeResponseAt", "n8n")),
                node("end", AutomationNodeType.END, Map.of())
        ), List.of(edge("trigger",null,"pages"),edge("pages",null,"jdm"),edge("jdm",null,"n8n"),edge("n8n",null,"end")));

        runtime.run(execution, definition);

        assertEquals(2, AutomationDataSupport.list(execution.getOutput().get("rows")).size());
        assertEquals("APPROVE", AutomationDataSupport.readPath(execution.getOutput(), "decision.route"));
        assertEquals("n8n", AutomationDataSupport.readPath(execution.getOutput(), "n8n.provider"));
    }

    @Test
    void parentWaitsForPersistedSubflowAndResumesWithItsOutput() {
        Map<String, AutomationExecution> stored = new LinkedHashMap<>();
        when(executions.save(any(AutomationExecution.class))).thenAnswer(invocation -> {
            AutomationExecution saved = invocation.getArgument(0);
            stored.put(saved.getExecutionId(), saved);
            return saved;
        });
        when(executions.findByExecutionId(anyString())).thenAnswer(invocation ->
                Optional.ofNullable(stored.get(invocation.getArgument(0))));

        AutomationFlowDefinition childDefinition = definition(List.of(
                node("child-trigger", AutomationNodeType.WEBHOOK_TRIGGER, Map.of()),
                node("child-wait", AutomationNodeType.WAIT, Map.of("delaySeconds", 1)),
                node("child-map", AutomationNodeType.MAP_FIELDS, Map.of("mappings", Map.of("child.done", true))),
                node("child-end", AutomationNodeType.END, Map.of())
        ), List.of(
                edge("child-trigger", null, "child-wait"),
                edge("child-wait", null, "child-map"),
                edge("child-map", null, "child-end")
        ));
        childDefinition.setFlowKey("child-flow");
        childDefinition.setEntryNodeId("child-trigger");
        when(flows.active("tenant", "site", "child-flow", "default")).thenReturn(childDefinition);

        AutomationFlowDefinition parentDefinition = definition(List.of(
                node("trigger", AutomationNodeType.WEBHOOK_TRIGGER, Map.of()),
                node("subflow", AutomationNodeType.SUBFLOW, Map.of("flowKey", "child-flow", "resultPath", "childResult")),
                node("end", AutomationNodeType.END, Map.of())
        ), List.of(edge("trigger", null, "subflow"), edge("subflow", null, "end")));
        AutomationExecution parent = execution(Map.of("caseId", "c-1"));
        stored.put(parent.getExecutionId(), parent);

        runtime.run(parent, parentDefinition);

        assertEquals("WAITING", parent.getStatus());
        String childId = String.valueOf(AutomationDataSupport.map(parent.getContext().get("subflowExecutions")).get("subflow"));
        AutomationExecution child = stored.get(childId);
        assertEquals("WAITING", child.getStatus());
        assertTrue(child.getInlineFragment().containsKey("nodes"));

        child.setStatus("RUNNING");
        child.setCurrentNodeId(child.getResumeNodeId());
        child.setResumeAt(null);
        child.setResumeNodeId(null);
        runtime.run(child, childDefinition);
        executions.save(child);

        parent.setStatus("RUNNING");
        parent.setCurrentNodeId(parent.getResumeNodeId());
        parent.setResumeAt(null);
        parent.setResumeNodeId(null);
        runtime.run(parent, parentDefinition);

        assertEquals("COMPLETED", parent.getStatus());
        assertEquals(true, AutomationDataSupport.readPath(parent.getOutput(), "childResult.child.done"));
        assertFalse(parent.getContext().containsKey("subflowExecutions"));
    }

    private AutomationExecution execution(Map<String,Object> variables) { AutomationExecution value=new AutomationExecution();value.setExecutionId("exec-test");value.setTenantKey("tenant");value.setSiteKey("site");value.setStatus("RUNNING");value.setInput(new LinkedHashMap<>(variables));value.setOutput(new LinkedHashMap<>(variables));value.setContext(new LinkedHashMap<>());return value; }
    private AutomationNode node(String id,AutomationNodeType type,Map<String,Object> config){return new AutomationNode(id,type,id,true,null,null,null,null,null,config,null,null);}
    private AutomationEdge edge(String from,String port,String to){return new AutomationEdge(from+"-"+to,from,port,to);}
    private AutomationFlowDefinition definition(List<AutomationNode> nodes,List<AutomationEdge> edges){AutomationFlowDefinition value=new AutomationFlowDefinition();value.setFlowKey("test-flow");value.setVersion(1);value.setEntryNodeId("trigger");value.setNodes(nodes);value.setEdges(edges);return value;}
}
