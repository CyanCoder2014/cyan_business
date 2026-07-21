package com.cyancoder.automationorchestrator.service;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class AutomationScheduleServiceTest {
    private final AutomationScheduleService schedules = new AutomationScheduleService(
            mock(AutomationFlowDefinitionService.class), mock(AutomationExecutionService.class));

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
}
