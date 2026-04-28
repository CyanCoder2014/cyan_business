package com.cyancoder.event.service;

import com.cyancoder.event.entity.BusinessEvent;
import com.cyancoder.event.model.BusinessEventRequest;
import com.cyancoder.event.repository.BusinessEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
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
        BusinessEvent event = new BusinessEvent();
        event.setEventKey(request.eventKey() == null || request.eventKey().isBlank() ? UUID.randomUUID().toString() : request.eventKey());
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
}
