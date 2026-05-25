package com.cyancoder.notification.model;

import java.util.Map;

public record QueuedNotificationMessage(
        String messageKey,
        String channel,
        String templateKey,
        String recipient,
        String subject,
        String body,
        String provider,
        Map<String, Object> model,
        Map<String, Object> relatedRef
) {
}
