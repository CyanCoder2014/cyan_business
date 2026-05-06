package com.cyancoder.checkout.model;

import java.util.Map;

public record NotificationDispatchRequest(
        String messageKey,
        String channel,
        String templateKey,
        String recipient,
        String subject,
        String body,
        Map<String, Object> model,
        Map<String, Object> relatedRef
) {
}
