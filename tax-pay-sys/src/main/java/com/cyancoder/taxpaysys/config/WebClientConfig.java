package com.cyancoder.taxpaysys.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

//    @Bean
//    public ServiceInstanceListSupplier instanceSupplier(ConfigurableApplicationContext context) {
//        return ServiceInstanceListSupplier.builder()
//                .withDiscoveryClient()
//                .withHealthChecks()
//                .build(context);
//    }
}
