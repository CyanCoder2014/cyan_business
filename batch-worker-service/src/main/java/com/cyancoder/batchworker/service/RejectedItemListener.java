package com.cyancoder.batchworker.service;

import com.cyancoder.batchworker.domain.BatchRejectedItem;
import com.cyancoder.batchworker.repository.BatchRejectedItemRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.batch.core.listener.SkipListener;

public class RejectedItemListener implements SkipListener<Map<String, Object>, Map<String, Object>> {
    private final UUID runId;
    private final BatchRejectedItemRepository repository;
    private final ObjectMapper objectMapper;

    public RejectedItemListener(UUID runId, BatchRejectedItemRepository repository, ObjectMapper objectMapper) {
        this.runId = runId;
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onSkipInWrite(Map<String, Object> item, Throwable cause) {
        BatchRejectedItem rejected = new BatchRejectedItem();
        rejected.setRunId(runId);
        rejected.setPayloadJson(truncate(json(item), 8000));
        rejected.setReason(truncate(cause.getMessage(), 2000));
        rejected.setCreatedAt(Instant.now());
        repository.save(rejected);
    }

    private String json(Map<String, Object> item) {
        try {
            return objectMapper.writeValueAsString(item);
        } catch (JsonProcessingException exception) {
            return "{\"serializationError\":true}";
        }
    }

    private String truncate(String value, int length) {
        if (value == null) return "";
        return value.substring(0, Math.min(value.length(), length));
    }
}
