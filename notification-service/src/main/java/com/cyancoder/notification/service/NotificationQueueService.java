package com.cyancoder.notification.service;

import com.cyancoder.notification.model.QueuedNotificationMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationQueueService {
    private final KafkaTemplate<String, QueuedNotificationMessage> kafkaTemplate;
    private final NotificationDispatchService notificationDispatchService;

    public NotificationQueueService(KafkaTemplate<String, QueuedNotificationMessage> kafkaTemplate,
                                    NotificationDispatchService notificationDispatchService) {
        this.kafkaTemplate = kafkaTemplate;
        this.notificationDispatchService = notificationDispatchService;
    }

    public void enqueue(QueuedNotificationMessage message) {
        kafkaTemplate.send("notification-dispatch", message.messageKey(), message);
    }

    @KafkaListener(topics = "notification-dispatch", groupId = "notification-service")
    public void consume(QueuedNotificationMessage message) {
        notificationDispatchService.processQueued(message);
    }
}
