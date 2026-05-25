package com.cyancoder.event.service;

import com.cyancoder.event.entity.BusinessEvent;
import com.cyancoder.event.repository.BusinessEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class BusinessEventKafkaDispatcher {

    private final BusinessEventRepository repository;
    private final BusinessEventService businessEventService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public BusinessEventKafkaDispatcher(
            BusinessEventRepository repository,
            BusinessEventService businessEventService,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${business.events.topic}") String topic
    ) {
        this.repository = repository;
        this.businessEventService = businessEventService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    @Scheduled(fixedDelayString = "${event.kafka.dispatch.fixed-delay-ms:5000}")
    public void dispatchPending() {
        for (BusinessEvent event : repository.findTop50ByKafkaPublishedFalseOrderByOccurredAtAsc()) {
            event.setKafkaLastAttemptAt(Instant.now());
            event.setKafkaAttemptCount(event.getKafkaAttemptCount() + 1);
            try {
                kafkaTemplate.send(topic, event.getEntityKey(), objectMapper.writeValueAsString(businessEventService.toEnvelope(event)));
                event.setKafkaPublished(true);
                event.setKafkaPublishedAt(Instant.now());
            } catch (Exception ex) {
                event.setKafkaPublished(false);
            }
            repository.save(event);
        }
    }
}
