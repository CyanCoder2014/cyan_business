package com.cyancoder.bpm.service;

import com.cyancoder.bpm.api.dto.BpmScope;
import com.cyancoder.bpm.domain.DynamicFlowDefinition;
import com.cyancoder.bpm.domain.SubmitMode;
import com.cyancoder.bpm.repo.DynamicFlowDefinitionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ImporterCreditDeliveryBpmScenarioTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void savesCreditReviewWithDynamicFormProcessorAndTerminalOutcomes() throws Exception {
        DynamicFlowDefinition flow = fixture("customer-credit-review-bpm-flow.json");
        DynamicFlowDefinition saved = save(flow);

        assertThat(saved.getTenantKey()).isEqualTo("importer-demo");
        assertThat(saved.getSiteKey()).isEqualTo("main-site");
        assertThat(saved.getVersion()).isEqualTo(1);
        assertThat(saved.getStartState()).isEqualTo("finance-review");
        var review = saved.getStates().stream()
                .filter(state -> "finance-review".equals(state.id()))
                .findFirst().orElseThrow();
        assertThat(review.formKey()).isEqualTo("customer-credit-review");
        assertThat(review.entityKey()).isEqualTo("customer-credit-review");
        assertThat(review.processorKey()).isEqualTo("credit-review-normalizer");
        assertThat(review.submitMode()).isEqualTo(SubmitMode.DYNAMIC);
        assertThat(review.candidateGroups()).contains("finance-credit-reviewers");
        assertThat(saved.getTransitions())
                .extracting(transition -> transition.toState())
                .containsExactlyInAnyOrder("approved", "rejected");
    }

    @Test
    void savesDeliveryExceptionFlowForQuarantinedBatchItems() throws Exception {
        DynamicFlowDefinition saved = save(fixture("delivery-exception-bpm-flow.json"));

        assertThat(saved.getStartState()).isEqualTo("operations-review");
        var review = saved.getStates().stream()
                .filter(state -> "operations-review".equals(state.id()))
                .findFirst().orElseThrow();
        assertThat(review.formKey()).isEqualTo("delivery-exception");
        assertThat(review.entityKey()).isEqualTo("delivery-exception");
        assertThat(review.submitMode()).isEqualTo(SubmitMode.DYNAMIC);
        assertThat(review.candidateGroups()).contains("delivery-operators");
        assertThat(saved.getStates().stream().filter(state -> state.terminal()).count()).isEqualTo(1);
    }

    private DynamicFlowDefinition save(DynamicFlowDefinition flow) {
        DynamicFlowDefinitionRepository repository = mock(DynamicFlowDefinitionRepository.class);
        DynamicFlowDeploymentService deployment = mock(DynamicFlowDeploymentService.class);
        when(repository.findByTenantKeyAndSiteKeyOrderByFlowKeyAscVersionDesc(
                "importer-demo", "main-site")).thenReturn(List.of());
        when(repository.findByTenantKeyAndSiteKeyAndFlowKeyOrderByVersionDesc(
                "importer-demo", "main-site", flow.getFlowKey())).thenReturn(List.of());
        when(repository.save(any(DynamicFlowDefinition.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(deployment.isAvailable()).thenReturn(false);
        return new FlowDefinitionService(repository, deployment)
                .save(new BpmScope("importer-demo", "main-site"), flow);
    }

    private DynamicFlowDefinition fixture(String name) throws Exception {
        return mapper.readValue(Files.readString(scenarioFile(name)), DynamicFlowDefinition.class);
    }

    private Path scenarioFile(String name) {
        Path fromModule = Path.of("..", "docs", "examples", "importer-credit-delivery", name);
        return Files.exists(fromModule)
                ? fromModule
                : Path.of("docs", "examples", "importer-credit-delivery", name);
    }
}
