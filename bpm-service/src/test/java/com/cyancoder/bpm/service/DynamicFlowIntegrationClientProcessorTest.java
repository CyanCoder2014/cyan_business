package com.cyancoder.bpm.service;

import com.cyancoder.bpm.api.dto.BpmScope;
import com.cyancoder.bpm.api.dto.FormSubmissionSyncRequest;
import com.cyancoder.bpm.api.dto.ProcessorRunResponse;
import com.cyancoder.bpm.domain.FlowState;
import com.cyancoder.bpm.domain.SubmitMode;
import com.cyancoder.dynamiccore.store.mongo.DynamicEntityRecordDocument;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DynamicFlowIntegrationClientProcessorTest {

    @Test
    void processorOutputIsValidatedAndPersisted() {
        InternalServiceHttpSupport http = mock(InternalServiceHttpSupport.class);
        when(http.post(eq("processor-service"), eq("/api/processor-service/processors/normalize/run"), any(), eq("tenant"), eq("site"), eq(ProcessorRunResponse.class)))
                .thenReturn(new ProcessorRunResponse(true, java.util.List.of(), Map.of("name", "Jane")));
        DynamicEntityRecordDocument saved = new DynamicEntityRecordDocument();
        saved.setRecordKey("record-1");
        saved.setData(Map.of("name", "Jane"));
        when(http.post(eq("content-service"), eq("/internal/entities/records/form-a"), any(), eq("tenant"), eq("site"), eq(DynamicEntityRecordDocument.class)))
                .thenReturn(saved);

        var response = new DynamicFlowIntegrationClient(http).submitForm(state("normalize"), request(), new BpmScope("tenant", "site"));

        assertThat(response.currentFormValues()).containsEntry("name", "Jane");
    }

    @Test
    void processorFailureStopsPersistence() {
        InternalServiceHttpSupport http = mock(InternalServiceHttpSupport.class);
        when(http.post(eq("processor-service"), any(), any(), any(), any(), eq(ProcessorRunResponse.class)))
                .thenReturn(new ProcessorRunResponse(false, java.util.List.of("name is required"), Map.of()));

        assertThatThrownBy(() -> new DynamicFlowIntegrationClient(http).submitForm(state("normalize"), request(), new BpmScope("tenant", "site")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name is required");
    }

    private FlowState state(String processorKey) {
        return new FlowState("draft", "Draft", false, "form-a", processorKey, false, java.util.Set.of(), java.util.List.of(),
                null, "content-service", "form-a", null, null, SubmitMode.DYNAMIC, null, false);
    }

    private FormSubmissionSyncRequest request() {
        return new FormSubmissionSyncRequest("obj", "APPLICATION", "flow", "draft", "form-a", "normalize", null,
                "user", Map.of("name", " jane "), Map.of(), Map.of());
    }
}
