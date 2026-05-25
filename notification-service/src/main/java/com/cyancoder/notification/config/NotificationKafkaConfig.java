package com.cyancoder.notification.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationKafkaConfig {

    @Bean
    public NewTopic notificationDispatchTopic() {
        return new NewTopic("notification-dispatch", 1, (short) 1);
    }
}
