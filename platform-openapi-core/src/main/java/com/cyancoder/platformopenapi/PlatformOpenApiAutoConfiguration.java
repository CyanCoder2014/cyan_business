package com.cyancoder.platformopenapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;

@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnClass(OpenAPI.class)
@ConditionalOnProperty(prefix = "platform.openapi", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(PlatformOpenApiProperties.class)
@Import({
        PlatformOpenApiDocsSecurityConfiguration.class,
        PlatformReactiveOpenApiDocsSecurityConfiguration.class
})
public class PlatformOpenApiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI platformOpenApi(
            PlatformOpenApiProperties properties,
            @Value("${spring.application.name:platform-service}") String applicationName
    ) {
        String title = properties.getTitle() == null || properties.getTitle().isBlank()
                ? applicationName
                : properties.getTitle();
        OpenAPI openApi = new OpenAPI()
                .info(new Info()
                        .title(title)
                        .description(properties.getDescription())
                        .version(properties.getVersion()));
        openApi.addExtension("x-platform-service-key", applicationName);
        if (properties.getServerUrl() != null && !properties.getServerUrl().isBlank()) {
            openApi.setServers(List.of(new Server().url(properties.getServerUrl())));
        }
        return openApi;
    }

    @Bean
    public OpenApiCustomizer platformOpenApiCustomizer(PlatformOpenApiProperties properties) {
        return new PlatformOpenApiCustomizer(properties);
    }

    @Bean
    public PlatformOperationAuthCustomizer platformOperationAuthCustomizer() {
        return new PlatformOperationAuthCustomizer();
    }
}
