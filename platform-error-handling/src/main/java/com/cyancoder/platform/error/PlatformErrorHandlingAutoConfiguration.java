package com.cyancoder.platform.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class PlatformErrorHandlingAutoConfiguration {

    @Bean
    public PlatformErrorLocalizationService platformErrorLocalizationService(ObjectMapper objectMapper) {
        return new PlatformErrorLocalizationService(objectMapper);
    }

    @Bean
    public PlatformExceptionHandler platformExceptionHandler(PlatformErrorLocalizationService localizationService) {
        return new PlatformExceptionHandler(localizationService);
    }
}
