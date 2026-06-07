package com.cyancoder.content;

import com.cyancoder.dynamiccore.config.DynamicCoreConfig;
import com.cyancoder.dynamiccore.config.DynamicPersistenceConfig;
import com.cyancoder.dynamiccore.security.DualApiSecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = {"com.cyancoder.content.entity", "com.cyancoder.dynamiccore.store.jpa"})
@EnableJpaRepositories(basePackages = {"com.cyancoder.content.repository", "com.cyancoder.dynamiccore.store.jpa"})
@EnableMongoRepositories(basePackages = {"com.cyancoder.dynamiccore.store.mongo"})
@Import({DynamicCoreConfig.class, DynamicPersistenceConfig.class, DualApiSecurityConfig.class})
public class ContentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentServiceApplication.class, args);
    }
}
