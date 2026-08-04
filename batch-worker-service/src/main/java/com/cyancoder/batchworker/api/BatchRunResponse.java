package com.cyancoder.batchworker.api;

import com.cyancoder.batchworker.domain.BatchRun;
import com.cyancoder.batchworker.domain.BatchRunStatus;
import java.time.Instant;
import java.util.UUID;

public record BatchRunResponse(
        UUID id, String tenantKey, String siteKey, String definitionKey, String runKey,
        BatchRunStatus status, Long batchExecutionId, long readCount, long writeCount,
        long skipCount, String errorMessage, Instant createdAt, Instant startedAt, Instant completedAt
) {
    public static BatchRunResponse from(BatchRun value) {
        return new BatchRunResponse(value.getId(), value.getTenantKey(), value.getSiteKey(),
                value.getDefinitionKey(), value.getRunKey(), value.getStatus(),
                value.getBatchExecutionId(), value.getReadCount(), value.getWriteCount(),
                value.getSkipCount(), value.getErrorMessage(), value.getCreatedAt(),
                value.getStartedAt(), value.getCompletedAt());
    }
}
