package com.cyancoder.inventoryautomation.service;

import com.cyancoder.inventoryautomation.entity.InventoryAutomationAction;
import com.cyancoder.inventoryautomation.model.BusinessEventEnvelope;
import com.cyancoder.inventoryautomation.repository.InventoryAutomationActionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class InventoryAutomationConsumer {

    private final InventoryAutomationActionRepository repository;
    private final ObjectMapper objectMapper;

    public InventoryAutomationConsumer(InventoryAutomationActionRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${business.events.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onEvent(String message) throws Exception {
        BusinessEventEnvelope event = objectMapper.readValue(message, BusinessEventEnvelope.class);
        if (!supports(event) || repository.findByEventKey(event.eventKey()).isPresent()) {
            return;
        }
        InventoryAutomationAction action = new InventoryAutomationAction();
        action.setEventKey(event.eventKey());
        action.setEntityType(event.entityType());
        action.setEntityKey(event.entityKey());
        action.setActionType(event.actionType());
        action.setAutomationType("STOCK_SIDE_EFFECT");
        action.setSummary(event.entityType() + " " + event.actionType() + " captured for inventory automation");
        action.setPayloadJson(objectMapper.writeValueAsString(event.payload()));
        repository.save(action);
    }

    private boolean supports(BusinessEventEnvelope event) {
        return "INVENTORY".equalsIgnoreCase(event.entityType())
                || "COMMERCE".equalsIgnoreCase(event.entityType())
                || "CATALOG".equalsIgnoreCase(event.entityType());
    }
}
