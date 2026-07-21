package com.cyancoder.automationorchestrator.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class N8nExpressionServiceTest {
    private final N8nExpressionService expressions = new N8nExpressionService();

    @Test
    void evaluatesItemInputWorkflowAndPriorNodeExpressions() {
        Map<String, Object> item = Map.of("json", Map.of("price", 4, "name", "cyan"));
        N8nExpressionService.Evaluation evaluation = new N8nExpressionService.Evaluation(
                item, AutomationDataSupport.map(item.get("json")), Map.of(), List.of(item), 0,
                Map.of("tax", 2), Map.of("id", "exec-1"), Map.of("name", "flow"),
                Map.of("Lookup", List.of(Map.of("json", Map.of("status", "READY"))))
        );

        assertEquals(10.0, expressions.materialize("={{ $json.price * 2 + $vars.tax }}", evaluation));
        assertEquals("CYAN", expressions.materialize("={{ $json.name.toUpperCase() }}", evaluation));
        assertEquals("READY", expressions.materialize("={{ $('Lookup').first().json.status }}", evaluation));
        assertEquals("case-cyan-0", expressions.materialize("case-{{$json.name}}-{{$itemIndex}}", evaluation));
    }
}
