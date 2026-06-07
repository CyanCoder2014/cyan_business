package com.cyancoder.payment;

import com.cyancoder.payment.config.InternalSecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.cyancoder.payment.entity")
@EnableJpaRepositories(basePackages = "com.cyancoder.payment.repository")
@EnableConfigurationProperties({InternalSecurityProperties.class})
public class PaymentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
