package com.cyancoder.notification.sender;

import com.cyancoder.notification.model.NotificationDispatchRequest;
import com.cyancoder.notification.model.NotificationSendResult;
import org.springframework.stereotype.Component;

@Component
public class SmsKavenegarNotificationSender implements NotificationSender {
    @Override
    public boolean supports(String channel, String provider) {
        return "SMS".equalsIgnoreCase(channel) && (provider == null || provider.isBlank() || "kavenegar".equalsIgnoreCase(provider) || "default".equalsIgnoreCase(provider));
    }

    @Override
    public NotificationSendResult send(NotificationDispatchRequest request, String subject, String body) {
        return new NotificationSendResult(true, "kavenegar", "sms-" + request.messageKey(), "SENT", "");
    }
}
