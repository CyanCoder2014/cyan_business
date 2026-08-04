package com.cyancoder.apidocs;

import com.cyancoder.apidocs.config.ApiDocsCatalogProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(ApiDocsCatalogProperties.class)
public class ApiDocsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiDocsServiceApplication.class, args);
    }

    @Bean
    ObjectMapper apiDocsObjectMapper() {
        return new ObjectMapper();
    }
}
