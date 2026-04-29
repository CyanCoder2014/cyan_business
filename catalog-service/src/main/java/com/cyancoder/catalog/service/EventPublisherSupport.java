package com.cyancoder.catalog.service;

import com.cyancoder.catalog.entity.CatalogOutboxEvent;
import com.cyancoder.catalog.repository.CatalogOutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class EventPublisherSupport {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;
    private final String eventServiceBaseUrl;
    private final CatalogOutboxEventRepository repository;

    public EventPublisherSupport(
            ObjectMapper objectMapper,
            @Value("${event.service.base-url:http://localhost:9109}") String eventServiceBaseUrl,
            CatalogOutboxEventRepository repository
    ) {
        this.objectMapper = objectMapper;
        this.eventServiceBaseUrl = eventServiceBaseUrl;
        this.repository = repository;
    }

    public void publish(String sourceService, String entityType, String entityKey, String actionType, String title, Object payload) {
        CatalogOutboxEvent event = new CatalogOutboxEvent();
        event.setEventKey(UUID.randomUUID().toString());
        event.setSourceService(sourceService);
        event.setEntityType(entityType);
        event.setEntityKey(entityKey);
        event.setActionType(actionType);
        event.setTitle(title);
        try {
            event.setPayloadJson(objectMapper.writeValueAsString(payload));
        } catch (Exception ex) {
            throw new IllegalArgumentException("failed to serialize outbox payload", ex);
        }
        repository.save(event);
    }

    @Scheduled(fixedDelayString = "${outbox.dispatch.fixed-delay-ms:5000}")
    public void dispatchPending() {
        for (CatalogOutboxEvent event : repository.findTop20ByStatusInOrderByCreatedAtAsc(List.of("PENDING", "FAILED"))) {
            event.setLastAttemptAt(Instant.now());
            event.setAttemptCount(event.getAttemptCount() + 1);
            try {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("eventKey", event.getEventKey());
                body.put("sourceService", event.getSourceService());
                body.put("entityType", event.getEntityType());
                body.put("entityKey", event.getEntityKey());
                body.put("actionType", event.getActionType());
                body.put("title", event.getTitle());
                body.put("occurredAt", event.getCreatedAt());
                body.put("payload", objectMapper.readValue(event.getPayloadJson(), Map.class));
                restTemplate.exchange(eventServiceBaseUrl + "/api/event-service/events", HttpMethod.POST, new HttpEntity<>(body), Void.class);
                event.setStatus("DELIVERED");
                event.setDeliveredAt(Instant.now());
            } catch (Exception ex) {
                event.setStatus("FAILED");
            }
            repository.save(event);
        }
    }
}
