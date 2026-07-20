package com.cyancoder.automationorchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AutomationOrchestratorServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AutomationOrchestratorServiceApplication.class, args);
    }
}
