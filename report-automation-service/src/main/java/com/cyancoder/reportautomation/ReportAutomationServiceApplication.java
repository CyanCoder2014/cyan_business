package com.cyancoder.reportautomation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class ReportAutomationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReportAutomationServiceApplication.class, args);
    }
}
