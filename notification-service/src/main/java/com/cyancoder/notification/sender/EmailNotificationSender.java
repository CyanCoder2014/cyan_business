package com.cyancoder.notification.sender;

import com.cyancoder.notification.model.NotificationDispatchRequest;
import com.cyancoder.notification.model.NotificationSendResult;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationSender implements NotificationSender {
    @Override
    public boolean supports(String channel, String provider) {
        return "EMAIL".equalsIgnoreCase(channel) && (provider == null || provider.isBlank() || "default".equalsIgnoreCase(provider) || "smtp".equalsIgnoreCase(provider));
    }

    @Override
    public NotificationSendResult send(NotificationDispatchRequest request, String subject, String body) {
        return new NotificationSendResult(true, "smtp", "smtp-" + request.messageKey(), "SENT", "");
    }
}
