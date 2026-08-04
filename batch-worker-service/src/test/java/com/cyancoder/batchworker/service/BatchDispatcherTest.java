package com.cyancoder.batchworker.service;

import static org.mockito.Mockito.*;

import com.cyancoder.batchworker.domain.BatchRun;
import com.cyancoder.batchworker.domain.BatchRunStatus;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;

class BatchDispatcherTest {
    @Test
    void finalizesCompletedOrphanWithoutLaunchingDuplicateJob() {
        BatchRunClaimService claims = mock(BatchRunClaimService.class);
        BatchDefinitionService definitions = mock(BatchDefinitionService.class);
        BatchJobFactory jobs = mock(BatchJobFactory.class);
        JobOperator jobOperator = mock(JobOperator.class);
        JobRepository jobRepository = mock(JobRepository.class);
        BatchRunFinisher finisher = mock(BatchRunFinisher.class);
        BatchRun run = new BatchRun();
        run.setStatus(BatchRunStatus.RUNNING);
        run.setTenantKey("tenant");
        run.setSiteKey("site");
        run.setDefinitionKey("sync");
        run.setRunKey("2026-07-23");
        run.setBatchExecutionId(91L);
        setId(run, UUID.randomUUID());
        JobExecution completed = mock(JobExecution.class);
        when(completed.getStatus()).thenReturn(BatchStatus.COMPLETED);
        when(jobRepository.getJobExecution(91L)).thenReturn(completed);
        when(claims.claim(anyString())).thenReturn(Optional.of(new BatchRunClaimService.Claim(run, true)));
        BatchDispatcher dispatcher = new BatchDispatcher(
                claims, definitions, jobs, jobOperator, jobRepository, finisher);

        dispatcher.dispatch();

        verify(finisher).finish(eq(run.getId()), anyString(), same(completed), isNull());
        verifyNoInteractions(jobOperator, jobs, definitions);
    }

    private void setId(BatchRun run, UUID id) {
        try {
            var field = BatchRun.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(run, id);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
