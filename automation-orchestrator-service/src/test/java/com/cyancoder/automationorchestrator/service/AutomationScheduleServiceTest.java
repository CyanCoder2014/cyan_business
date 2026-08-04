package com.cyancoder.automationorchestrator.service;

import com.cyancoder.automationorchestrator.domain.AutomationFlowDefinition;
import com.cyancoder.automationorchestrator.domain.AutomationNode;
import com.cyancoder.automationorchestrator.domain.AutomationNodeType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import org.mockito.InOrder;

class AutomationScheduleServiceTest {
    private final AutomationFlowDefinitionService definitions = mock(AutomationFlowDefinitionService.class);
    private final AutomationExecutionService executions = mock(AutomationExecutionService.class);
    private final AutomationScheduleService schedules = new AutomationScheduleService(definitions, executions);

    @Test
    void calculatesNativeAndImportedN8nIntervals() {
        Instant start = Instant.parse("2026-07-21T00:00:00Z");
        assertEquals(start.plusSeconds(30), schedules.next(Map.of("intervalSeconds", 30), start));
        assertEquals(start.plusSeconds(120), schedules.next(Map.of(
                "rule", Map.of("interval", List.of(Map.of("field", "minutes", "minutesInterval", 2)))
        ), start));
    }

    @Test
    void calculatesCronInConfiguredTimezone() {
        Instant start = Instant.parse("2026-07-21T00:00:01Z");
        assertEquals(Instant.parse("2026-07-21T00:05:00Z"), schedules.next(Map.of(
                "cron", "0 */5 * * * *", "timezone", "UTC"
        ), start));
    }

    @Test
    void createsIdempotentExecutionBeforeAdvancingScheduleCheckpoint() {
        AutomationFlowDefinition definition = new AutomationFlowDefinition();
        definition.setTenantKey("tenant");
        definition.setSiteKey("site");
        definition.setFlowKey("morning-sync");
        definition.setEnvironment("default");
        definition.setEntryNodeId("trigger");
        definition.setNextScheduledAt(Instant.now().minusSeconds(1));
        definition.setNodes(List.of(new AutomationNode(
                "trigger", AutomationNodeType.SCHEDULE_TRIGGER, "trigger", true,
                null, null, null, null, null, Map.of("intervalSeconds", 60), null, null)));
        when(definitions.activeScheduledCandidates()).thenReturn(List.of(definition));

        schedules.triggerDueFlows();

        InOrder order = inOrder(executions, definitions);
        order.verify(executions).triggerWebhook(
                org.mockito.ArgumentMatchers.eq("morning-sync"),
                org.mockito.ArgumentMatchers.eq("tenant"),
                org.mockito.ArgumentMatchers.eq("site"),
                org.mockito.ArgumentMatchers.anyMap(),
                org.mockito.ArgumentMatchers.anyMap(),
                org.mockito.ArgumentMatchers.startsWith("schedule:morning-sync:"));
        order.verify(definitions).saveScheduleState(definition);
    }
}
