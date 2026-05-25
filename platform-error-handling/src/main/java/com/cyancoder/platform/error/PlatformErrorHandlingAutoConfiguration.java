package com.cyancoder.platform.error;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class PlatformErrorHandlingAutoConfiguration {

    @Bean
    public PlatformErrorLocalizationService platformErrorLocalizationService() {
        return new PlatformErrorLocalizationService();
    }

    @Bean
    public PlatformExceptionHandler platformExceptionHandler(PlatformErrorLocalizationService localizationService) {
        return new PlatformExceptionHandler(localizationService);
    }
}
