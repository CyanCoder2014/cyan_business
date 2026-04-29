package com.cyancoder.event.service;

import com.cyancoder.event.entity.BusinessEvent;
import com.cyancoder.event.model.BusinessEventEnvelope;
import com.cyancoder.event.model.BusinessEventRequest;
import com.cyancoder.event.repository.BusinessEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BusinessEventService {

    private final BusinessEventRepository repository;
    private final ObjectMapper objectMapper;

    public BusinessEventService(BusinessEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public BusinessEvent publish(BusinessEventRequest request) {
        String eventKey = request.eventKey() == null || request.eventKey().isBlank() ? UUID.randomUUID().toString() : request.eventKey();
        BusinessEvent existing = repository.findByEventKey(eventKey).orElse(null);
        if (existing != null) {
            return existing;
        }
        BusinessEvent event = new BusinessEvent();
        event.setEventKey(eventKey);
        event.setSourceService(request.sourceService());
        event.setEntityType(request.entityType());
        event.setEntityKey(request.entityKey());
        event.setActionType(request.actionType());
        event.setTitle(request.title());
        event.setOccurredAt(request.occurredAt() == null ? Instant.now() : request.occurredAt());
        try {
            event.setPayloadJson(objectMapper.writeValueAsString(request.payload()));
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid event payload", ex);
        }
        return repository.save(event);
    }

    public List<BusinessEvent> list(String sourceService, String entityType, String entityKey, String actionType) {
        if (sourceService != null && !sourceService.isBlank()) {
            return repository.findBySourceServiceOrderByOccurredAtDesc(sourceService);
        }
        if (entityType != null && !entityType.isBlank()) {
            return repository.findByEntityTypeOrderByOccurredAtDesc(entityType);
        }
        if (entityKey != null && !entityKey.isBlank()) {
            return repository.findByEntityKeyOrderByOccurredAtDesc(entityKey);
        }
        if (actionType != null && !actionType.isBlank()) {
            return repository.findByActionTypeOrderByOccurredAtDesc(actionType);
        }
        return repository.findAll().stream().sorted((left, right) -> right.getOccurredAt().compareTo(left.getOccurredAt())).toList();
    }

    public BusinessEventEnvelope toEnvelope(BusinessEvent event) {
        try {
            Map<String, Object> payload = event.getPayloadJson() == null || event.getPayloadJson().isBlank()
                    ? Map.of()
                    : objectMapper.readValue(event.getPayloadJson(), Map.class);
            return new BusinessEventEnvelope(
                    event.getEventKey(),
                    event.getSourceService(),
                    event.getEntityType(),
                    event.getEntityKey(),
                    event.getActionType(),
                    event.getTitle(),
                    event.getOccurredAt(),
                    payload
            );
        } catch (Exception ex) {
            throw new IllegalStateException("failed to map business event envelope", ex);
        }
    }
}
