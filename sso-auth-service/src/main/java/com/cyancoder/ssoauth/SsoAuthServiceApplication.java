package com.cyancoder.ssoauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SsoAuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SsoAuthServiceApplication.class, args);
    }
}
