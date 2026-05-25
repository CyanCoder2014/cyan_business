package com.cyancoder.notification.sender;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationSenderRegistry {
    private final List<NotificationSender> senders;

    public NotificationSenderRegistry(List<NotificationSender> senders) {
        this.senders = senders;
    }

    public NotificationSender resolve(String channel, String provider) {
        return senders.stream()
                .filter(sender -> sender.supports(channel, provider))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No notification sender for channel=" + channel + ", provider=" + provider));
    }
}
