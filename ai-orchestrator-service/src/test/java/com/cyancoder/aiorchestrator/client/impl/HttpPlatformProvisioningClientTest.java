package com.cyancoder.aiorchestrator.client.impl;

import com.cyancoder.aiorchestrator.exception.DownstreamServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HttpPlatformProvisioningClientTest {
    private final InternalServiceHttpSupport http = mock(InternalServiceHttpSupport.class);
    private final HttpPlatformProvisioningClient client =
            new HttpPlatformProvisioningClient(http, new ObjectMapper());

    @Test
    void updatesProcessorWhenCreateReportsExistingDefinition() {
        Map<String, Object> body = Map.of("processorKey", "normalize-order");
        when(http.post("processor-service", "/api/processor-service/processors",
                body, "tenant", "site"))
                .thenThrow(downstream(500));
        when(http.put("processor-service", "/api/processor-service/processors/normalize-order",
                body, "tenant", "site"))
                .thenReturn("{\"processorKey\":\"normalize-order\"}");

        Map<String, Object> result = client.upsertResource(
                "PROCESSOR_DEFINITION", "processor-service", "normalize-order",
                body, "tenant", "site");

        assertThat(result).containsEntry("processorKey", "normalize-order");
    }

    @Test
    void replacesExistingAutomationVersionUsingItsMongoIdentity() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("flowKey", "morning-sync");
        body.put("version", 2);
        body.put("entryNodeId", "schedule");

        when(http.post("automation-orchestrator-service", "/internal/automation-flows",
                body, "tenant", "site"))
                .thenThrow(downstream(500));
        when(http.get("automation-orchestrator-service",
                "/internal/automation-flows/morning-sync/versions/2", "tenant", "site"))
                .thenReturn("{\"id\":\"mongo-id\",\"revision\":4}");
        when(http.post("automation-orchestrator-service", "/internal/automation-flows",
                Map.of(
                        "flowKey", "morning-sync",
                        "version", 2,
                        "entryNodeId", "schedule",
                        "id", "mongo-id",
                        "revision", 4
                ), "tenant", "site"))
                .thenReturn("{\"flowKey\":\"morning-sync\",\"version\":2}");

        Map<String, Object> result = client.upsertResource(
                "AUTOMATION_FLOW", "automation-orchestrator-service", "morning-sync",
                body, "tenant", "site");

        assertThat(result).containsEntry("flowKey", "morning-sync");
        verify(http).get("automation-orchestrator-service",
                "/internal/automation-flows/morning-sync/versions/2", "tenant", "site");
    }

    private DownstreamServiceException downstream(int status) {
        return new DownstreamServiceException(
                "downstream error", "service", "/path", status, "error", null);
    }
}
