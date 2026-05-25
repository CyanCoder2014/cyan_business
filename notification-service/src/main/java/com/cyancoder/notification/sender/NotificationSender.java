package com.cyancoder.notification.sender;

import com.cyancoder.notification.model.NotificationDispatchRequest;
import com.cyancoder.notification.model.NotificationSendResult;

public interface NotificationSender {
    boolean supports(String channel, String provider);

    NotificationSendResult send(NotificationDispatchRequest request, String subject, String body);
}
