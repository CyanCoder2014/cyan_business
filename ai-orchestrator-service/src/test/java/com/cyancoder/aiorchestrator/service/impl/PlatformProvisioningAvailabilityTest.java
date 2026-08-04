package com.cyancoder.aiorchestrator.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.cyancoder.aiorchestrator.client.PlatformProvisioningClient;
import com.cyancoder.aiorchestrator.domain.FlowBlueprint;
import com.cyancoder.aiorchestrator.domain.PlatformAppDslDefinition;
import com.cyancoder.aiorchestrator.domain.PlatformResourceBlueprint;
import com.cyancoder.aiorchestrator.repo.ProvisioningRunRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PlatformProvisioningAvailabilityTest {
    @Test
    void provisionsAllowListedProcessorWithoutCreatingUnavailableStorefrontTheme() {
        PlatformProvisioningClient client = mock(PlatformProvisioningClient.class);
        when(client.upsertResource(anyString(), anyString(), anyString(), anyMap(), anyString(), anyString()))
                .thenReturn(Map.of("processorKey", "normalize-order"));
        PlatformProvisioningService service = new PlatformProvisioningService(
                client, mock(ProvisioningRunRepository.class));
        PlatformAppDslDefinition dsl = new PlatformAppDslDefinition();
        dsl.getApp().setTenantKey("tenant");
        dsl.getApp().setSiteKey("site");
        dsl.getApp().setAvailableServiceKeys(List.of("processor-service"));
        PlatformResourceBlueprint processor = new PlatformResourceBlueprint();
        processor.setResourceType("PROCESSOR_DEFINITION");
        processor.setServiceKey("processor-service");
        processor.setResourceKey("normalize-order");
        processor.setBody(Map.of(
                "processorKey", "normalize-order",
                "validatorsJson", "[]",
                "operatorsJson", "[]",
                "active", true));
        dsl.setResources(List.of(processor));

        var result = service.provision(dsl);

        assertThat(result.createdResources()).containsExactly(Map.of("processorKey", "normalize-order"));
        verify(client).upsertResource(eq("PROCESSOR_DEFINITION"), eq("processor-service"),
                eq("normalize-order"), anyMap(), eq("tenant"), eq("site"));
        verify(client, never()).createDefinitionFromTemplate(eq("storefront-service"), anyString(),
                anyString(), anyString(), anyString());
        verify(client, never()).createRecord(eq("storefront-service"), anyString(),
                anyString(), anyMap(), anyString(), anyString());
    }

    @Test
    void provisionsProcessorBeforeBpmFlowThatMayReferenceIt() {
        PlatformProvisioningClient client = mock(PlatformProvisioningClient.class);
        when(client.upsertResource(anyString(), anyString(), anyString(), anyMap(), anyString(), anyString()))
                .thenReturn(Map.of("processorKey", "normalize-order"));
        when(client.createBpmFlow(anyMap(), anyString(), anyString()))
                .thenReturn(Map.of("flowKey", "order-flow"));
        PlatformProvisioningService service = new PlatformProvisioningService(
                client, mock(ProvisioningRunRepository.class));
        PlatformAppDslDefinition dsl = new PlatformAppDslDefinition();
        dsl.getApp().setTenantKey("tenant");
        dsl.getApp().setSiteKey("site");
        dsl.getApp().setAvailableServiceKeys(List.of("processor-service", "bpm-service"));

        PlatformResourceBlueprint processor = new PlatformResourceBlueprint();
        processor.setResourceType("PROCESSOR_DEFINITION");
        processor.setServiceKey("processor-service");
        processor.setResourceKey("normalize-order");
        processor.setBody(Map.of("processorKey", "normalize-order"));
        dsl.setResources(List.of(processor));

        FlowBlueprint flow = new FlowBlueprint();
        flow.setFlowKey("order-flow");
        flow.setFlowDefinition(Map.of("flowKey", "order-flow"));
        dsl.setFlows(List.of(flow));

        service.provision(dsl);

        var order = inOrder(client);
        order.verify(client).upsertResource(eq("PROCESSOR_DEFINITION"), eq("processor-service"),
                eq("normalize-order"), anyMap(), eq("tenant"), eq("site"));
        order.verify(client).createBpmFlow(anyMap(), eq("tenant"), eq("site"));
    }
}
