package com.cyancoder.event.config;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaTopicConfigTest {

    private final KafkaTopicConfig config = new KafkaTopicConfig();

    @Test
    void createsStringProducerAndKafkaTemplate() {
        ProducerFactory<String, String> producerFactory =
                config.businessEventProducerFactory("localhost:9092");

        KafkaTemplate<String, String> kafkaTemplate =
                config.businessEventKafkaTemplate(producerFactory);

        assertThat(producerFactory).isNotNull();
        assertThat(kafkaTemplate).isNotNull();

        kafkaTemplate.destroy();
    }
}
