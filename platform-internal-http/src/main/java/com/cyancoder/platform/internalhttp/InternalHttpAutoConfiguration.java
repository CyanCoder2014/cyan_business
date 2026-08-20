package com.cyancoder.platform.internalhttp;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@AutoConfiguration
public class InternalHttpAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public InternalServiceCredentialsResolver internalServiceCredentialsResolver(Environment environment) {
        return new InternalServiceCredentialsResolver(environment);
    }
}
