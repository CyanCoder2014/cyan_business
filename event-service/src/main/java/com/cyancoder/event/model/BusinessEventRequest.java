package com.cyancoder.event.model;

import java.time.Instant;
import java.util.Map;

public record BusinessEventRequest(
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
