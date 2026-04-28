package com.cyancoder.event.service;

import com.cyancoder.event.entity.BusinessEvent;
import com.cyancoder.event.model.BusinessEventEnvelope;
import com.cyancoder.event.model.BusinessEventRequest;
import com.cyancoder.event.repository.BusinessEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class BusinessEventService {

    private final BusinessEventRepository repository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public BusinessEventService(
            BusinessEventRepository repository,
            ObjectMapper objectMapper,
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${business.events.topic}") String topic
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
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
        BusinessEvent saved = repository.save(event);
        publishToKafka(saved, request.payload());
        return saved;
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

    private void publishToKafka(BusinessEvent event, java.util.Map<String, Object> payload) {
        try {
            BusinessEventEnvelope envelope = new BusinessEventEnvelope(
                    event.getEventKey(),
                    event.getSourceService(),
                    event.getEntityType(),
                    event.getEntityKey(),
                    event.getActionType(),
                    event.getTitle(),
                    event.getOccurredAt(),
                    payload
            );
            kafkaTemplate.send(topic, event.getEntityKey(), objectMapper.writeValueAsString(envelope));
        } catch (Exception ex) {
            throw new IllegalStateException("failed to publish business event to kafka", ex);
        }
    }
}
