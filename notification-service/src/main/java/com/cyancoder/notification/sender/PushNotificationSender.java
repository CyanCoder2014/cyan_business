package com.cyancoder.notification.sender;

import com.cyancoder.notification.model.NotificationDispatchRequest;
import com.cyancoder.notification.model.NotificationSendResult;
import org.springframework.stereotype.Component;

@Component
public class PushNotificationSender implements NotificationSender {
    @Override
    public boolean supports(String channel, String provider) {
        return "PUSH".equalsIgnoreCase(channel);
    }

    @Override
    public NotificationSendResult send(NotificationDispatchRequest request, String subject, String body) {
        return new NotificationSendResult(false, request.provider() == null || request.provider().isBlank() ? "push-default" : request.provider(), "", "NOT_CONFIGURED", "Push provider is not configured");
    }
}
