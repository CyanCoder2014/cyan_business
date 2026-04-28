package com.cyancoder.crmautomation.service;

import com.cyancoder.crmautomation.entity.CrmAutomationAction;
import com.cyancoder.crmautomation.model.BusinessEventEnvelope;
import com.cyancoder.crmautomation.repository.CrmAutomationActionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class CrmAutomationConsumer {

    private final CrmAutomationActionRepository repository;
    private final ObjectMapper objectMapper;

    public CrmAutomationConsumer(CrmAutomationActionRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${business.events.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onEvent(String message) throws Exception {
        BusinessEventEnvelope event = objectMapper.readValue(message, BusinessEventEnvelope.class);
        if (!supports(event)) {
            return;
        }
        if (repository.findByEventKey(event.eventKey()).isPresent()) {
            return;
        }
        CrmAutomationAction action = new CrmAutomationAction();
        action.setEventKey(event.eventKey());
        action.setEntityType(event.entityType());
        action.setEntityKey(event.entityKey());
        action.setActionType(event.actionType());
        action.setAutomationType(resolveAutomationType(event));
        action.setSummary(resolveSummary(event));
        action.setPayloadJson(objectMapper.writeValueAsString(event.payload()));
        repository.save(action);
    }

    private boolean supports(BusinessEventEnvelope event) {
        return "CRM".equalsIgnoreCase(event.entityType())
                || "COMMERCE".equalsIgnoreCase(event.entityType());
    }

    private String resolveAutomationType(BusinessEventEnvelope event) {
        if ("CRM".equalsIgnoreCase(event.entityType())) {
            return "LEAD_PIPELINE_UPDATE";
        }
        return "CUSTOMER_ACTIVITY_SYNC";
    }

    private String resolveSummary(BusinessEventEnvelope event) {
        return event.entityType() + " " + event.actionType() + " received from " + event.sourceService();
    }
}
