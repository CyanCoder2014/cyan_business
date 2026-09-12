package com.cyancoder.automationorchestrator.service;

import com.cyancoder.automationorchestrator.domain.*;
import com.cyancoder.automationorchestrator.repo.AutomationExecutionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ParallelSubflowSupportTest {
    @Test
    void persistsStableChildrenThenJoinsOnlyAfterEveryBranchCompletes() {
        AutomationExecution parent = new AutomationExecution();
        parent.setExecutionId("parent-1"); parent.setTenantKey("tenant"); parent.setSiteKey("site");
        parent.setOutput(new LinkedHashMap<>(Map.of("caseKey", "case-1"))); parent.setContext(Map.of());
        AutomationNode node = new AutomationNode("fanout", AutomationNodeType.PARALLEL_SUBFLOWS, "fanout", true, null, null, null, null, null,
                Map.of("branches", Map.of("one", Map.of("flowKey", "lookup", "input", Map.of("caseKey", "{{caseKey}}")),
                        "two", Map.of("flowKey", "lookup", "input", Map.of("caseKey", "{{caseKey}}"))), "maxConcurrency", 2), null, null);
        AutomationFlowDefinition child = new AutomationFlowDefinition();
        child.setFlowKey("lookup"); child.setVersion(1); child.setEntryNodeId("start"); child.setRuntimeMode("VARIABLES");
        AutomationFlowDefinitionService flows = mock(AutomationFlowDefinitionService.class);
        when(flows.active(any(), any(), eq("lookup"), any())).thenReturn(child);
        AutomationExecutionRepository repository = mock(AutomationExecutionRepository.class);
        Map<String, AutomationExecution> saved = new LinkedHashMap<>();
        when(repository.findByExecutionId(any())).thenAnswer(call -> Optional.ofNullable(saved.get(call.getArgument(0))));
        when(repository.save(any(AutomationExecution.class))).thenAnswer(call -> { AutomationExecution item = call.getArgument(0); saved.put(item.getExecutionId(), item); return item; });

        ParallelSubflowSupport.Result waiting = ParallelSubflowSupport.advance(parent, node, flows, repository, new ObjectMapper(), null);
        assertFalse(waiting.complete());
        assertEquals(2, saved.size());
        assertTrue(saved.values().stream().allMatch(childExecution -> "WAITING".equals(childExecution.getStatus())));
        saved.values().forEach(childExecution -> { childExecution.setStatus("COMPLETED"); childExecution.setOutput(Map.of("ok", true)); });

        ParallelSubflowSupport.Result complete = ParallelSubflowSupport.advance(parent, node, flows, repository, new ObjectMapper(), null);
        assertTrue(complete.complete());
        assertEquals(Map.of("ok", true), complete.outputs().get("one"));
        assertEquals(Map.of("ok", true), complete.outputs().get("two"));
    }
}
