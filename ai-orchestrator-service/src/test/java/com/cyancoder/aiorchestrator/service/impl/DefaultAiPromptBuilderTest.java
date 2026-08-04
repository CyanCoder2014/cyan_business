package com.cyancoder.aiorchestrator.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DefaultAiPromptBuilderTest {
    @Test
    void includesAvailabilityAndCrossServiceOrchestrationContracts() {
        String prompt = new DefaultAiPromptBuilder().buildPlatformPrompt(
                "Create a morning approval import",
                Map.of("_serviceAvailability", Map.of(
                        "source", "REQUEST",
                        "availableServiceKeys", List.of("bpm-service", "processor-service"))),
                List.of(), "tenant", "site");

        assertThat(prompt)
                .contains("_serviceAvailability.availableServiceKeys is authoritative")
                .contains("processor first, then target entity strict validation, then save")
                .contains("submitMode DYNAMIC requires entityService and entityKey")
                .contains("SCHEDULE_TRIGGER -> RUN_BATCH_JOB -> END")
                .contains("Do not claim exactly-once remote HTTP effects")
                .contains("PROCESSOR_DEFINITION")
                .contains("AUTOMATION_FLOW")
                .contains("BATCH_DEFINITION");
    }
}
