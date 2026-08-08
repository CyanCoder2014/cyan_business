package com.cyancoder.automationorchestrator.service;

import com.cyancoder.automationorchestrator.domain.*;
import com.cyancoder.automationorchestrator.repo.AutomationExecutionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AutomationAiOperationTest {
    @Test void executesAiOperationThroughAiOrchestratorInternalContract() {
        InternalServiceHttpSupport http = mock(InternalServiceHttpSupport.class);
        when(http.internalHeaders(anyString(), any(), any())).thenReturn(new org.springframework.http.HttpHeaders());
        when(http.exchange(eq("ai-orchestrator-service"), eq("/internal/ai-orchestrator/operations"), eq(HttpMethod.POST), any(), any(), eq(Object.class)))
                .thenReturn(Map.of("status", "COMPLETED", "output", Map.of("summary", "done")));
        AutomationExecutionRepository executions = mock(AutomationExecutionRepository.class);
        when(executions.findByExecutionId(anyString())).thenReturn(Optional.empty());
        GraphAutomationRuntime runtime = new GraphAutomationRuntime(http, mock(ConnectorCredentialService.class),
                mock(AutomationFlowDefinitionService.class), executions, mock(GoRulesDecisionService.class));
        AutomationFlowDefinition flow = new AutomationFlowDefinition();
        flow.setFlowKey("ai-data-flow"); flow.setEntryNodeId("trigger"); flow.setRuntimeMode("VARIABLES");
        flow.setNodes(List.of(
                new AutomationNode("trigger", AutomationNodeType.MANUAL_TRIGGER, "Manual", true, null, null, null, null, null, Map.of(), Map.of(), null),
                new AutomationNode("ai", AutomationNodeType.AI_OPERATION, "Summarize", true, null, null, null, null, null,
                        Map.of("operation", "TRANSFORM_DATA", "instructions", "Summarize", "input", "${variables}", "resultPath", "ai"), Map.of(), null),
                new AutomationNode("end", AutomationNodeType.END, "End", true, null, null, null, null, null, Map.of(), Map.of(), null)
        ));
        flow.setEdges(List.of(new AutomationEdge("e1", "trigger", null, "ai", null), new AutomationEdge("e2", "ai", null, "end", null)));
        AutomationExecution execution = new AutomationExecution(); execution.setExecutionId("exec-1"); execution.setTenantKey("tenant-a"); execution.setSiteKey("site-a"); execution.setInput(new LinkedHashMap<>()); execution.setOutput(new LinkedHashMap<>()); execution.setContext(new LinkedHashMap<>());

        runtime.run(execution, flow);

        assertEquals("COMPLETED", execution.getStatus());
        assertEquals("done", ((Map<?, ?>) execution.getOutput().get("ai")).get("summary"));
        verify(http).exchange(eq("ai-orchestrator-service"), eq("/internal/ai-orchestrator/operations"), eq(HttpMethod.POST), any(), any(), eq(Object.class));
    }
}
