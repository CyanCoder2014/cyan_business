package com.cyancoder.dynamiccore.runtime;

import com.cyancoder.dynamiccore.config.DynamicRuntimeProperties;
import com.cyancoder.dynamiccore.store.mongo.DynamicEntityRecordDocument;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DynamicEntityRecordPaginationTest {

    @Test
    void endpointReturnsPageEnvelopeWhenPagingIsRequested() {
        DynamicRuntimeService runtime = mock(DynamicRuntimeService.class);
        DynamicEntityRecordDocument order = new DynamicEntityRecordDocument();
        order.setRecordKey("order-100");
        DynamicScope scope = new DynamicScope("demo-tenant", "main-site");
        when(runtime.listRecords("importer-order", scope, 2, 500, "createdAt,asc"))
                .thenReturn(new PageImpl<>(List.of(order), PageRequest.of(2, 500), 1001));

        Object result = new EndpointDynamicEntityController(
                runtime, new DynamicRuntimeProperties(), mock(DynamicEntityResponseMapper.class), null)
                .listRecords(
                        "demo-tenant", "main-site", "importer-order",
                        2, 500, "createdAt,asc");

        DynamicPageResponse<?> page = (DynamicPageResponse<?>) result;
        assertThat(page.content()).hasSize(1);
        assertThat(page.content().get(0)).isSameAs(order);
        assertThat(page.page()).isEqualTo(2);
        assertThat(page.size()).isEqualTo(500);
        assertThat(page.totalElements()).isEqualTo(1001);
    }

    @Test
    void internalEndpointKeepsLegacyArrayWhenPagingIsNotRequested() {
        DynamicRuntimeService runtime = mock(DynamicRuntimeService.class);
        DynamicScope scope = new DynamicScope("demo-tenant", "main-site");
        DynamicEntityRecordDocument order = new DynamicEntityRecordDocument();
        when(runtime.listRecords("importer-order", scope)).thenReturn(List.of(order));

        Object result = new InternalDynamicEntityController(
                runtime, mock(DynamicEntityResponseMapper.class), null)
                .listRecords(
                        "demo-tenant", "main-site", "importer-order",
                        null, null, null);

        assertThat(result).isEqualTo(List.of(order));
        verify(runtime).listRecords("importer-order", scope);
    }
}
