package com.cyancoder.automationorchestrator.service;

import com.cyancoder.automationorchestrator.domain.AutomationFlowDefinition;
import com.cyancoder.automationorchestrator.domain.AutomationNodeType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class N8nWorkflowCompatibilityServiceTest {
    private final N8nWorkflowCompatibilityService compatibility =
            new N8nWorkflowCompatibilityService(mock(AutomationFlowDefinitionService.class));

    @Test
    void importsAndExportsCoreN8nWorkflowWithoutCredentials() {
        Map<String, Object> workflow = Map.of(
                "name", "Imported Flow",
                "active", false,
                "nodes", List.of(
                        Map.of("id", "n1", "name", "Manual", "type", "n8n-nodes-base.manualTrigger", "position", List.of(0, 0), "parameters", Map.of()),
                        Map.of("id", "n2", "name", "Set", "type", "n8n-nodes-base.set", "position", List.of(200, 0),
                                "parameters", Map.of("values", Map.of("string", List.of(Map.of("name", "status", "value", "READY")))))
                ),
                "connections", Map.of("Manual", Map.of("main", List.of(List.of(Map.of("node", "Set", "type", "main", "index", 0)))))
        );

        AutomationFlowDefinition imported = compatibility.convert("imported", workflow);

        assertEquals("N8N_ITEMS", imported.getRuntimeMode());
        assertEquals(AutomationNodeType.MANUAL_TRIGGER, imported.getNodes().getFirst().type());
        assertEquals("READY", AutomationDataSupport.readPath(imported.getNodes().get(1).configOrEmpty(), "assignments.status"));
        assertEquals(1, imported.getEdges().size());
        Map<String, Object> exported = compatibility.export(imported);
        assertEquals(2, AutomationDataSupport.list(exported.get("nodes")).size());
        assertTrue(AutomationDataSupport.map(exported.get("connections")).containsKey("Manual"));
    }

    @Test
    void reportsAppSpecificConnectorAsUnsupported() {
        Map<String, Object> analysis = compatibility.analyze(Map.of("nodes", List.of(
                Map.of("name", "Slack", "type", "n8n-nodes-base.slack")
        )));
        assertEquals(false, analysis.get("compatible"));
        assertEquals(1, AutomationDataSupport.list(analysis.get("unsupportedNodes")).size());
    }
}
