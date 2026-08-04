package com.cyancoder.automationorchestrator.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PipelineAutomationRuntimeTest {

    @Test
    void runsForEachScriptMappingsAndApiCalls() {
        InternalServiceHttpSupport httpSupport = mock(InternalServiceHttpSupport.class);
        when(httpSupport.internalHeaders(any(), eq("tenant"), eq("site"))).thenReturn(new HttpHeaders());
        when(httpSupport.exchange(eq("risk-service"), eq("/internal/risk/check"), eq(HttpMethod.POST), any(), any(), eq(Map.class)))
                .thenReturn(Map.of("decision", "APPROVE", "score", 18));
        when(httpSupport.exchange(eq("report-service"), eq("/internal/entities/records/automation-result"), eq(HttpMethod.POST), any(), any(), eq(Map.class)))
                .thenReturn(Map.of("recordKey", "result-1"));

        PipelineAutomationRuntime runtime = new PipelineAutomationRuntime(httpSupport);
        Map<String, Object> result = runtime.execute(Map.of(
                "type", "PIPELINE",
                "steps", List.of(
                        Map.of(
                                "type", "FOR_EACH",
                                "sourcePath", "documents",
                                "steps", List.of(Map.of(
                                        "type", "SCRIPT",
                                        "expression", "#item['size'] * 2",
                                        "targetPath", "weightedSize"
                                )),
                                "resultPath", "weightedSize",
                                "targetPath", "weightedSizes"
                        ),
                        Map.of("type", "MAP_FIELDS", "mappings", Map.of("riskRequest.sizes", "weightedSizes")),
                        Map.of(
                                "type", "CALL_API",
                                "serviceKey", "risk-service",
                                "path", "/internal/risk/check",
                                "body", "{{riskRequest}}",
                                "responseMappings", Map.of("risk.decision", "decision", "risk.score", "score")
                        ),
                        Map.of(
                                "type", "SAVE",
                                "serviceKey", "report-service",
                                "path", "/internal/entities/records/automation-result",
                                "body", Map.of("data", "{{risk}}"),
                                "storeResponseAt", "saveResponse"
                        )
                )
        ), Map.of("documents", List.of(Map.of("size", 10), Map.of("size", 20))), "tenant", "site");

        assertThat(result).extractingByKey("weightedSizes").isEqualTo(List.of(20, 40));
        assertThat(result).extractingByKey("risk").isEqualTo(Map.of("decision", "APPROVE", "score", 18));
        assertThat(result).extractingByKey("saveResponse").isEqualTo(Map.of("recordKey", "result-1"));
        verify(httpSupport).exchange(eq("report-service"), eq("/internal/entities/records/automation-result"), eq(HttpMethod.POST), any(), any(), eq(Map.class));
    }
}
