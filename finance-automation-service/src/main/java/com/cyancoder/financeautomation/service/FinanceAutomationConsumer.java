package com.cyancoder.financeautomation.service;

import com.cyancoder.financeautomation.entity.FinanceAutomationAction;
import com.cyancoder.financeautomation.model.BusinessEventEnvelope;
import com.cyancoder.financeautomation.repository.FinanceAutomationActionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class FinanceAutomationConsumer {

    private final FinanceAutomationActionRepository repository;
    private final ObjectMapper objectMapper;

    public FinanceAutomationConsumer(FinanceAutomationActionRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${business.events.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onEvent(String message) throws Exception {
        BusinessEventEnvelope event = objectMapper.readValue(message, BusinessEventEnvelope.class);
        if (!supports(event) || repository.findByEventKey(event.eventKey()).isPresent()) {
            return;
        }
        FinanceAutomationAction action = new FinanceAutomationAction();
        action.setEventKey(event.eventKey());
        action.setEntityType(event.entityType());
        action.setEntityKey(event.entityKey());
        action.setActionType(event.actionType());
        action.setAutomationType("FINANCE_SETTLEMENT_SYNC");
        action.setSummary(event.entityType() + " " + event.actionType() + " captured for finance automation");
        action.setPayloadJson(objectMapper.writeValueAsString(event.payload()));
        repository.save(action);
    }

    private boolean supports(BusinessEventEnvelope event) {
        return "FINANCE".equalsIgnoreCase(event.entityType())
                || "COMMERCE".equalsIgnoreCase(event.entityType());
    }
}
