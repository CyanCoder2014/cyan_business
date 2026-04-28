package com.cyancoder.reportautomation.model;

import java.time.Instant;
import java.util.Map;

public record BusinessEventEnvelope(
        String eventKey,
        String sourceService,
        String entityType,
        String entityKey,
        String actionType,
        String title,
        Instant occurredAt,
        Map<String, Object> payload
) {
}
