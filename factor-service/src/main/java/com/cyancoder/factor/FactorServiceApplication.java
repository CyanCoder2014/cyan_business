package com.cyancoder.factor;

import com.cyancoder.dynamiccore.config.DynamicCoreConfig;
import com.cyancoder.dynamiccore.config.DynamicPersistenceConfig;
import com.cyancoder.dynamiccore.security.DualApiSecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"com.cyancoder.factor.entity", "com.cyancoder.dynamiccore.store.jpa"})
@EnableJpaRepositories(basePackages = {"com.cyancoder.factor.repository", "com.cyancoder.dynamiccore.store.jpa"})
@EnableMongoRepositories(basePackages = {"com.cyancoder.dynamiccore.store.mongo"})
@Import({DynamicCoreConfig.class, DynamicPersistenceConfig.class, DualApiSecurityConfig.class})
public class FactorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FactorServiceApplication.class, args);
    }

}
