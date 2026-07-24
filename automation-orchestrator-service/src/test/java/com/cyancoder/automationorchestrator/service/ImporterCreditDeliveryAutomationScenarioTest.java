package com.cyancoder.automationorchestrator.service;

import com.cyancoder.automationorchestrator.domain.AutomationExecution;
import com.cyancoder.automationorchestrator.domain.AutomationFlowDefinition;
import com.cyancoder.automationorchestrator.domain.ConnectorCredential;
import com.cyancoder.automationorchestrator.repo.AutomationExecutionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ImporterCreditDeliveryAutomationScenarioTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final InternalServiceHttpSupport http = mock(InternalServiceHttpSupport.class);
    private final ConnectorCredentialService credentials = mock(ConnectorCredentialService.class);
    private final AutomationFlowDefinitionService flows = mock(AutomationFlowDefinitionService.class);
    private final AutomationExecutionRepository executions = mock(AutomationExecutionRepository.class);
    private final GoRulesDecisionService decisions =
            new GoRulesDecisionService(mapper, new DefaultResourceLoader());
    private final GraphAutomationRuntime runtime =
            new GraphAutomationRuntime(http, credentials, flows, executions, decisions);

    @Test
    void morningCreditMasterWaitsForDurableBatchThenFinalizesProjection() throws Exception {
        AutomationFlowDefinition flow = fixture("morning-credit-master-flow.json");
        new AutomationFlowDefinitionService(mock(com.cyancoder.automationorchestrator.repo.AutomationFlowDefinitionRepository.class), mapper)
                .validate(flow);
        AutomationExecution execution = execution(Map.of(
                "scheduledAt", "2026-07-24T04:30:00Z",
                "triggeredAt", "2026-07-24T04:30:01Z"));
        when(http.internalHeaders("batch-worker-service", "tenant", "site"))
                .thenReturn(new HttpHeaders());
        when(http.exchange(eq("batch-worker-service"), contains("/runs"), any(), any(), any(), eq(Object.class)))
                .thenReturn(Map.of("id", "order-run-1", "status", "QUEUED"))
                .thenReturn(Map.of(
                        "id", "order-run-1",
                        "status", "COMPLETED",
                        "readCount", 100_000,
                        "writeCount", 99_998,
                        "skipCount", 2));
        ConnectorCredential projectionCredential = new ConnectorCredential();
        projectionCredential.setType("BEARER");
        when(credentials.active("tenant", "site", "credit-projection-api"))
                .thenReturn(projectionCredential);
        when(credentials.secret(projectionCredential)).thenReturn("projection-token");
        when(http.exchangeUrl(eq("https://projection.example/v1/credit-projection/finalize"),
                eq(HttpMethod.POST), any(), any(), any(), any(), eq(Object.class)))
                .thenReturn(Map.of("customerCount", 8_400, "scoringExecutionsStarted", 8_400));

        runtime.run(execution, flow);
        assertThat(execution.getStatus()).isEqualTo("WAITING");

        resume(execution);
        runtime.run(execution, flow);

        assertThat(execution.getStatus()).isEqualTo("COMPLETED");
        assertThat(AutomationDataSupport.readPath(execution.getOutput(), "orderBatch.writeCount"))
                .isEqualTo(99_998);
        assertThat(AutomationDataSupport.readPath(execution.getOutput(), "projection.customerCount"))
                .isEqualTo(8_400);
        verify(http, times(1)).exchange(eq("batch-worker-service"),
                eq("/internal/batch/definitions/importer-order-projection-v1/runs"),
                eq(HttpMethod.POST), any(), any(), eq(Object.class));
    }

    @Test
    void customerCreditFlowExecutesRealZenPolicySavesReportAndOpensBpmReview() throws Exception {
        AutomationFlowDefinition flow = fixture("customer-credit-score-flow.json");
        new AutomationFlowDefinitionService(mock(com.cyancoder.automationorchestrator.repo.AutomationFlowDefinitionRepository.class), mapper)
                .validate(flow);
        AutomationExecution execution = execution(Map.of(
                "assessmentKey", "assessment-2026-07-24-customer-7",
                "customerKey", "customer-7",
                "assessedAt", "2026-07-24T05:00:00Z",
                "currency", "IRR",
                "sourceRunKey", "2026-07-24T04:30:00Z",
                "metrics", Map.of(
                        "orderCount90d", 40,
                        "activeDays90d", 25,
                        "orderFrequencyPerActiveDay", 1.6,
                        "totalAmount90d", 2_000_000_000L,
                        "averageOrderAmount90d", 50_000_000,
                        "maximumOverdueDays", 12,
                        "failedDeliveryCount90d", 6
                )));
        when(http.internalHeaders(anyString(), eq("tenant"), eq("site")))
                .thenReturn(new HttpHeaders());
        when(http.exchange(eq("report-service"), contains("/customer-credit-report/"),
                eq(HttpMethod.PUT), any(), any(), eq(Object.class)))
                .thenReturn(Map.of("recordKey", "assessment-2026-07-24-customer-7"));
        when(http.exchange(eq("bpm-service"), eq("/internal/bpm/managed-objects"),
                eq(HttpMethod.POST), any(), any(), eq(Object.class)))
                .thenReturn(Map.of("id", "bpm-credit-review-1", "state", "finance-review"));

        runtime.run(execution, flow);

        assertThat(execution.getStatus()).isEqualTo("COMPLETED");
        assertThat(AutomationDataSupport.readPath(execution.getOutput(), "decision.riskBand"))
                .isEqualTo("HIGH");
        assertThat(AutomationDataSupport.readPath(execution.getOutput(), "decision.manualReview"))
                .isEqualTo(true);
        assertThat(AutomationDataSupport.readPath(execution.getOutput(), "bpmReview.state"))
                .isEqualTo("finance-review");
        verify(http).exchange(eq("report-service"),
                eq("/internal/entities/records/customer-credit-report/assessment-2026-07-24-customer-7"),
                eq(HttpMethod.PUT), any(), any(), eq(Object.class));
        verify(http).exchange(eq("bpm-service"), eq("/internal/bpm/managed-objects"),
                eq(HttpMethod.POST), any(), any(), eq(Object.class));
    }

    @Test
    void deliveryFlowCreatesOneBpmCaseWhenBatchQuarantinesRejectedLoadings() throws Exception {
        AutomationFlowDefinition flow = fixture("morning-delivery-flow.json");
        new AutomationFlowDefinitionService(mock(com.cyancoder.automationorchestrator.repo.AutomationFlowDefinitionRepository.class), mapper)
                .validate(flow);
        AutomationExecution execution = execution(Map.of("scheduledAt", "2026-07-24T05:00:00Z"));
        when(http.internalHeaders(anyString(), eq("tenant"), eq("site")))
                .thenReturn(new HttpHeaders());
        when(http.exchange(eq("batch-worker-service"), contains("/runs"),
                any(), any(), any(), eq(Object.class)))
                .thenReturn(Map.of("id", "delivery-run-1", "status", "QUEUED"))
                .thenReturn(Map.of(
                        "id", "delivery-run-1",
                        "status", "COMPLETED",
                        "readCount", 12_500,
                        "writeCount", 12_497,
                        "skipCount", 3));
        when(http.exchange(eq("bpm-service"), eq("/internal/bpm/managed-objects"),
                eq(HttpMethod.POST), any(), any(), eq(Object.class)))
                .thenReturn(Map.of("id", "delivery-exception-1", "state", "operations-review"));

        runtime.run(execution, flow);
        resume(execution);
        runtime.run(execution, flow);

        assertThat(execution.getStatus()).isEqualTo("COMPLETED");
        assertThat(AutomationDataSupport.readPath(execution.getOutput(), "deliveryBatch.skipCount"))
                .isEqualTo(3);
        assertThat(AutomationDataSupport.readPath(execution.getOutput(), "deliveryException.state"))
                .isEqualTo("operations-review");
        verify(http).exchange(eq("bpm-service"), eq("/internal/bpm/managed-objects"),
                eq(HttpMethod.POST), any(), any(), eq(Object.class));
    }

    private AutomationFlowDefinition fixture(String name) throws Exception {
        return mapper.readValue(Files.readString(scenarioFile(name)), AutomationFlowDefinition.class);
    }

    private AutomationExecution execution(Map<String, Object> input) {
        AutomationExecution value = new AutomationExecution();
        value.setExecutionId("execution-" + input.hashCode());
        value.setTenantKey("tenant");
        value.setSiteKey("site");
        value.setStatus("RUNNING");
        value.setInput(new LinkedHashMap<>(input));
        value.setOutput(new LinkedHashMap<>(input));
        value.setContext(new LinkedHashMap<>());
        return value;
    }

    private void resume(AutomationExecution execution) {
        execution.setStatus("RUNNING");
        execution.setCurrentNodeId(execution.getResumeNodeId());
        execution.setResumeNodeId(null);
        execution.setResumeAt(null);
    }

    private Path scenarioFile(String name) {
        Path fromModule = Path.of("..", "docs", "examples", "importer-credit-delivery", name);
        return Files.exists(fromModule)
                ? fromModule
                : Path.of("docs", "examples", "importer-credit-delivery", name);
    }
}
