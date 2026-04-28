package com.cyancoder.reportautomation.service;

import com.cyancoder.reportautomation.entity.ReportAutomationRecord;
import com.cyancoder.reportautomation.model.BusinessEventEnvelope;
import com.cyancoder.reportautomation.repository.ReportAutomationRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ReportAutomationConsumer {

    private final ReportAutomationRecordRepository repository;
    private final ObjectMapper objectMapper;

    public ReportAutomationConsumer(ReportAutomationRecordRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${business.events.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onEvent(String message) throws Exception {
        BusinessEventEnvelope event = objectMapper.readValue(message, BusinessEventEnvelope.class);
        if (repository.findByEventKey(event.eventKey()).isPresent()) {
            return;
        }
        ReportAutomationRecord record = new ReportAutomationRecord();
        record.setEventKey(event.eventKey());
        record.setSourceService(event.sourceService());
        record.setEntityType(event.entityType());
        record.setEntityKey(event.entityKey());
        record.setActionType(event.actionType());
        record.setSummary(event.entityType() + " " + event.actionType() + " captured for report projection");
        record.setPayloadJson(objectMapper.writeValueAsString(event.payload()));
        repository.save(record);
    }
}
