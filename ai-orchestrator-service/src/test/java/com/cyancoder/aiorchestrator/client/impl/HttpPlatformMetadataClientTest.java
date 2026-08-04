package com.cyancoder.aiorchestrator.client.impl;

import com.cyancoder.aiorchestrator.config.PlatformMetadataProperties;
import com.cyancoder.aiorchestrator.service.ServiceAvailabilitySnapshot;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpPlatformMetadataClientTest {

    @Test
    void addsCompactLiveControllerApiMetadataWhenCatalogIsAvailable() {
        InternalServiceHttpSupport http = mock(InternalServiceHttpSupport.class);
        when(http.get(
                "api-docs-service",
                "/internal/api-docs/services/processor-service",
                "tenant",
                "site"))
                .thenReturn("""
                        {
                          "paths":{
                            "/api/processor-service/processors/{processorKey}/run":{
                              "post":{
                                "operationId":"run",
                                "summary":"Run processor",
                                "x-platform-auth":"BEARER"
                              }
                            }
                          }
                        }
                        """);
        HttpPlatformMetadataClient client = new HttpPlatformMetadataClient(
                http,
                new PlatformMetadataProperties(),
                new ObjectMapper());

        Map<String, Object> metadata = client.fetchMetadata(
                "tenant",
                "site",
                new ServiceAvailabilitySnapshot(
                        List.of("processor-service", "api-docs-service"),
                        "request"));

        @SuppressWarnings("unchecked")
        Map<String, Object> processor = (Map<String, Object>) metadata.get("processor-service");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> operations =
                (List<Map<String, Object>>) processor.get("controllerApis");
        assertThat(operations).containsExactly(Map.of(
                "method", "POST",
                "path", "/api/processor-service/processors/{processorKey}/run",
                "operationId", "run",
                "summary", "Run processor",
                "auth", "BEARER"));
    }

    @Test
    void doesNotCallCatalogWhenItIsNotInTheAvailableServiceInventory() {
        InternalServiceHttpSupport http = mock(InternalServiceHttpSupport.class);
        HttpPlatformMetadataClient client = new HttpPlatformMetadataClient(
                http,
                new PlatformMetadataProperties(),
                new ObjectMapper());

        Map<String, Object> metadata = client.fetchMetadata(
                "tenant",
                "site",
                new ServiceAvailabilitySnapshot(List.of("processor-service"), "request"));

        @SuppressWarnings("unchecked")
        Map<String, Object> processor = (Map<String, Object>) metadata.get("processor-service");
        assertThat(processor).doesNotContainKey("controllerApis");
    }
}
