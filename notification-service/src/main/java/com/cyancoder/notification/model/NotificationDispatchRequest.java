package com.cyancoder.notification.model;

import java.util.Map;

public record NotificationDispatchRequest(
        String messageKey,
        String channel,
        String templateKey,
        String provider,
        String dispatchMode,
        String recipient,
        String subject,
        String body,
        Map<String, Object> model,
        Map<String, Object> relatedRef
) {
}
