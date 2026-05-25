package com.cyancoder.financeautomation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class FinanceAutomationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinanceAutomationServiceApplication.class, args);
    }
}
