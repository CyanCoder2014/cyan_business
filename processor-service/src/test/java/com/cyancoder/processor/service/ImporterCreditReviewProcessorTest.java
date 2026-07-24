package com.cyancoder.processor.service;

import com.cyancoder.processor.entity.ProcessorDefinition;
import com.cyancoder.processor.model.ProcessorRunRequest;
import com.cyancoder.processor.repository.ProcessorDefinitionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ImporterCreditReviewProcessorTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void normalizesAndValidatesManualCreditReviewBeforeEntityPersistence() throws Exception {
        JsonNode fixture = mapper.readTree(Files.readString(scenarioFile("credit-review-processor.json")));
        ProcessorDefinition definition = new ProcessorDefinition();
        definition.setProcessorKey(fixture.path("processorKey").asText());
        definition.setTargetType(fixture.path("targetType").asText());
        definition.setValidatorsJson(fixture.path("validatorsJson").asText());
        definition.setOperatorsJson(fixture.path("operatorsJson").asText());
        definition.setActive(true);
        ProcessorDefinitionRepository repository = mock(ProcessorDefinitionRepository.class);
        when(repository.findByProcessorKey("credit-review-normalizer")).thenReturn(Optional.of(definition));
        ProcessorExecutionService service = new ProcessorExecutionService(repository, mapper);

        var valid = service.run("credit-review-normalizer", new ProcessorRunRequest(
                "BPM_FORM",
                Map.of(
                        "assessmentKey", "assessment-2026-07-24-c1",
                        "customerKey", " c1 ",
                        "approvedLimit", 500_000_000,
                        "reviewerDecision", "reduce",
                        "reviewerComment", "  approved after collateral review  "
                )));

        assertThat(valid.valid()).isTrue();
        assertThat(valid.payload())
                .containsEntry("customerKey", "c1")
                .containsEntry("reviewerDecision", "REDUCE")
                .containsEntry("reviewerComment", "approved after collateral review");

        var invalid = service.run("credit-review-normalizer", new ProcessorRunRequest(
                "BPM_FORM",
                Map.of(
                        "assessmentKey", "assessment-2026-07-24-c1",
                        "customerKey", "c1",
                        "approvedLimit", -1,
                        "reviewerDecision", "maybe",
                        "reviewerComment", " "
                )));

        assertThat(invalid.valid()).isFalse();
        assertThat(invalid.errors()).contains(
                "invalid reviewer decision",
                "approvedLimit cannot be negative",
                "reviewerComment is required");
    }

    private Path scenarioFile(String name) {
        Path fromModule = Path.of("..", "docs", "examples", "importer-credit-delivery", name);
        return Files.exists(fromModule)
                ? fromModule
                : Path.of("docs", "examples", "importer-credit-delivery", name);
    }
}
