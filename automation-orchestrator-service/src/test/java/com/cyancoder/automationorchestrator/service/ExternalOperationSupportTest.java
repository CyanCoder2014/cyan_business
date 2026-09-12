package com.cyancoder.automationorchestrator.service;

import com.cyancoder.automationorchestrator.domain.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ExternalOperationSupportTest {
    @Test
    void reconcilesBeforeInvokingAndKeepsStableIdempotencyKey() {
        AutomationExecution execution = new AutomationExecution();
        execution.setOutput(new LinkedHashMap<>(Map.of("operation", "op-42"))); execution.setContext(Map.of());
        AutomationNode node = new AutomationNode("transfer", AutomationNodeType.EXTERNAL_OPERATION, "transfer", true, null, null, null, null, null, Map.of(), null, null);
        Map<String, Object> config = Map.of("operationKey", "operation", "reconcile", Map.of("url", "https://provider/status"),
                "execute", Map.of("url", "https://provider/submit"));
        List<Map<String, Object>> calls = new ArrayList<>();
        ExternalOperationSupport.Result result = ExternalOperationSupport.execute(execution, node, config, (current, currentNode, request) -> {
            calls.add(request); return calls.size() == 1 ? Map.of("confirmed", false) : Map.of("confirmed", true, "reference", "r-1");
        }, null);
        assertTrue(result.confirmed());
        assertEquals(2, calls.size());
        assertEquals("op-42", AutomationDataSupport.map(calls.get(0).get("body")).get("operationKey"));
        assertEquals("op-42", AutomationDataSupport.map(calls.get(1).get("headers")).get("Idempotency-Key"));
        assertEquals("CONFIRMED", AutomationDataSupport.map(execution.getContext().get("externalOperations")).containsKey("transfer")
                ? AutomationDataSupport.map(AutomationDataSupport.map(execution.getContext().get("externalOperations")).get("transfer")).get("status") : null);
    }
}
