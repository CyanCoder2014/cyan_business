package com.cyancoder.dynamiccore.runtime;

import com.cyancoder.dynamiccore.config.DynamicRuntimeProperties;
import com.cyancoder.dynamiccore.store.jpa.StoredEntityDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DynamicEntityDefinitionPaginationTest {

    @Test
    void endpointReturnsPageMetadataAndMappedDefinitions() {
        DynamicRuntimeService runtime = mock(DynamicRuntimeService.class);
        DynamicEntityResponseMapper mapper = mock(DynamicEntityResponseMapper.class);
        StoredEntityDefinition stored = new StoredEntityDefinition();
        stored.setEntityKey("customer-credit-report");
        DynamicEntityDefinitionResponse response = new DynamicEntityDefinitionResponse(
                1L, "report-service", "demo-tenant", "main-site",
                "customer-credit-report", "REPORT", "Customer Credit Report",
                null, true, null, null);
        when(runtime.listDefinitions(
                new DynamicScope("demo-tenant", "main-site"), 1, 2, "title,desc"))
                .thenReturn(new PageImpl<>(List.of(stored), PageRequest.of(1, 2), 5));
        when(mapper.toDefinitionResponse(stored)).thenReturn(response);

        var page = new EndpointDynamicEntityController(
                runtime, new DynamicRuntimeProperties(), mapper)
                .listDefinitions("demo-tenant", "main-site", 1, 2, "title,desc");

        assertThat(page.content()).containsExactly(response);
        assertThat(page.page()).isEqualTo(1);
        assertThat(page.size()).isEqualTo(2);
        assertThat(page.totalElements()).isEqualTo(5);
        assertThat(page.totalPages()).isEqualTo(3);
    }

    @Test
    void internalEndpointUsesTheSameScopedPaginationContract() {
        DynamicRuntimeService runtime = mock(DynamicRuntimeService.class);
        DynamicEntityResponseMapper mapper = mock(DynamicEntityResponseMapper.class);
        when(runtime.listDefinitions(
                new DynamicScope("demo-tenant", "main-site"), 0, 20, "entityKey,asc"))
                .thenReturn(Page.empty(PageRequest.of(0, 20)));

        var page = new InternalDynamicEntityController(runtime, mapper)
                .listDefinitions(
                        "demo-tenant", "main-site", 0, 20, "entityKey,asc");

        assertThat(page.content()).isEmpty();
        assertThat(page.page()).isZero();
        assertThat(page.size()).isEqualTo(20);
        verify(runtime).listDefinitions(
                new DynamicScope("demo-tenant", "main-site"), 0, 20, "entityKey,asc");
    }
}
