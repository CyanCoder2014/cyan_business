package com.cyancoder.batchworker.service;

import com.cyancoder.batchworker.api.BatchDefinitionSpec;
import com.cyancoder.batchworker.config.BatchWorkerProperties;
import com.cyancoder.batchworker.domain.BatchRun;
import com.cyancoder.batchworker.repository.BatchRejectedItemRepository;
import com.cyancoder.batchworker.repository.BatchRunRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
public class BatchJobFactory {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ObjectMapper objectMapper;
    private final BatchRejectedItemRepository rejectedRepository;
    private final BatchRunRepository runRepository;
    private final BatchWorkerProperties defaults;

    public BatchJobFactory(JobRepository jobRepository, PlatformTransactionManager transactionManager,
            ObjectMapper objectMapper, BatchRejectedItemRepository rejectedRepository,
            BatchRunRepository runRepository,
            BatchWorkerProperties defaults) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.objectMapper = objectMapper;
        this.rejectedRepository = rejectedRepository;
        this.runRepository = runRepository;
        this.defaults = defaults;
    }

    public Job create(BatchRun run, BatchDefinitionSpec spec) {
        int chunkSize = positive(spec.chunkSize(), defaults.getChunkSize());
        int retries = positive(spec.retryLimit(), defaults.getRetryLimit());
        int skips = nonNegative(spec.skipLimit(), defaults.getSkipLimit());
        ApiBatchReader reader = new ApiBatchReader(spec.source(), objectMapper);
        String prefix = run.getTenantKey() + ":" + run.getSiteKey() + ":"
                + run.getDefinitionKey() + ":" + run.getRunKey();
        ApiBatchWriter writer = new ApiBatchWriter(spec.destination(), objectMapper, prefix);
        String suffix = digest(run.getTenantKey() + ":" + run.getSiteKey() + ":" + run.getDefinitionKey());

        ChunkOrientedStepBuilder<Map<String, Object>, Map<String, Object>> builder =
                new ChunkOrientedStepBuilder<>("api-etl-step-" + suffix, jobRepository, chunkSize);
        Step step = builder.reader(reader)
                .processor(item -> ApiBatchWriter.mapFields(item, spec.fieldMappings()))
                .writer(writer)
                .transactionManager(transactionManager)
                .faultTolerant()
                .retry(RetryableApiException.class)
                .retryLimit(retries)
                .skip(SkippableItemException.class)
                .skipLimit(skips)
                .skipListener(new RejectedItemListener(run.getId(), rejectedRepository, objectMapper))
                .build();
        return new JobBuilder("api-etl-" + suffix, jobRepository)
                .listener(new org.springframework.batch.core.listener.JobExecutionListener() {
                    @Override
                    @org.springframework.transaction.annotation.Transactional
                    public void beforeJob(org.springframework.batch.core.job.JobExecution execution) {
                        runRepository.recordBatchExecution(run.getId(), execution.getId());
                    }
                })
                .start(step).build();
    }

    private int positive(Integer value, int fallback) {
        return value == null || value < 1 ? fallback : value;
    }

    private int nonNegative(Integer value, int fallback) {
        return value == null || value < 0 ? fallback : value;
    }

    private String digest(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 24);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
