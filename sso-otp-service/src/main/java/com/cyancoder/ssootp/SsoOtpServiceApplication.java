package com.cyancoder.ssootp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SsoOtpServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SsoOtpServiceApplication.class, args);
    }
}
