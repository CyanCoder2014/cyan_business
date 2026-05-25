package com.cyancoder.crmautomation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class CrmAutomationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CrmAutomationServiceApplication.class, args);
    }
}
