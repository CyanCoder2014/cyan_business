package com.cyancoder.batchworker.controller;

import com.cyancoder.batchworker.domain.BatchRejectedItem;
import com.cyancoder.batchworker.domain.BatchRun;
import com.cyancoder.batchworker.repository.BatchRejectedItemRepository;
import com.cyancoder.batchworker.service.BatchDefinitionService;
import com.cyancoder.batchworker.service.BatchRunService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BatchControllerTest {
    @Test
    void listsRejectedItemsOnlyAfterCheckingRunScope() {
        BatchDefinitionService definitions = mock(BatchDefinitionService.class);
        BatchRunService runs = mock(BatchRunService.class);
        BatchRejectedItemRepository rejected = mock(BatchRejectedItemRepository.class);
        UUID runId = UUID.randomUUID();
        when(runs.get("tenant", "site", runId)).thenReturn(new BatchRun());
        BatchRejectedItem item = new BatchRejectedItem();
        item.setRunId(runId);
        item.setPayloadJson("{\"loadingKey\":\"loading-1\"}");
        item.setReason("Destination rejected item with HTTP 422");
        item.setCreatedAt(Instant.now());
        when(rejected.findAllByRunIdOrderByCreatedAtAsc(runId)).thenReturn(List.of(item));

        var result = new BatchController(definitions, runs, rejected)
                .rejectedItems("tenant", "site", runId);

        assertThat(result).containsExactly(item);
        verify(runs).get("tenant", "site", runId);
    }
}
