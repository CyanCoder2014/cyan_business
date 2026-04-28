package com.cyancoder.inventoryautomation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class InventoryAutomationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InventoryAutomationServiceApplication.class, args);
    }
}
