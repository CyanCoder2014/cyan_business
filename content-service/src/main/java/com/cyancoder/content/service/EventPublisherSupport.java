package com.cyancoder.content.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class EventPublisherSupport {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;
    private final String eventServiceBaseUrl;

    public EventPublisherSupport(ObjectMapper objectMapper, @Value("${event.service.base-url:http://localhost:9109}") String eventServiceBaseUrl) {
        this.objectMapper = objectMapper;
        this.eventServiceBaseUrl = eventServiceBaseUrl;
    }

    public void publish(String sourceService, String entityType, String entityKey, String actionType, String title, Object payload) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("eventKey", UUID.randomUUID().toString());
        body.put("sourceService", sourceService);
        body.put("entityType", entityType);
        body.put("entityKey", entityKey);
        body.put("actionType", actionType);
        body.put("title", title);
        body.put("occurredAt", Instant.now());
        body.put("payload", objectMapper.convertValue(payload, Map.class));
        restTemplate.exchange(
                eventServiceBaseUrl + "/api/event-service/events",
                HttpMethod.POST,
                new HttpEntity<>(body),
                Void.class
        );
    }
}
