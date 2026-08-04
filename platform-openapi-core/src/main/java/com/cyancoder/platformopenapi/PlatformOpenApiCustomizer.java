package com.cyancoder.platformopenapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;

import java.util.List;
import java.util.Locale;

public class PlatformOpenApiCustomizer implements OpenApiCustomizer {
    public static final String BEARER_SCHEME = "bearerAuth";
    public static final String BASIC_SCHEME = "basicAuth";

    private final PlatformOpenApiProperties properties;

    public PlatformOpenApiCustomizer(PlatformOpenApiProperties properties) {
        this.properties = properties;
    }

    @Override
    public void customise(OpenAPI openApi) {
        Components components = openApi.getComponents() == null
                ? new Components()
                : openApi.getComponents();
        components.addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Bearer access token"));
        components.addSecuritySchemes(BASIC_SCHEME, new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("basic")
                .description("Internal service credentials supplied at runtime"));
        openApi.setComponents(components);

        Paths paths = openApi.getPaths();
        if (paths == null) {
            return;
        }
        paths.forEach((path, item) -> item.readOperations().forEach(
                operation -> applySecurity(path, operation)));
    }

    private void applySecurity(String path, Operation operation) {
        if (operation.getExtensions() != null
                && operation.getExtensions().containsKey("x-platform-auth")) {
            return;
        }
        PlatformApiSecurity security = securityFor(path);
        applySecurity(operation, security);
    }

    static void applySecurity(Operation operation, PlatformApiSecurity security) {
        operation.addExtension("x-platform-auth", security.name());
        if (security == PlatformApiSecurity.NONE) {
            operation.setSecurity(List.of());
            return;
        }
        String scheme = security == PlatformApiSecurity.BASIC
                ? BASIC_SCHEME
                : BEARER_SCHEME;
        operation.setSecurity(List.of(new SecurityRequirement().addList(scheme)));
    }

    PlatformApiSecurity securityFor(String path) {
        String normalized = path == null ? "" : path.toLowerCase(Locale.ROOT);
        if (normalized.startsWith("/public/") || normalized.equals("/public")) {
            return PlatformApiSecurity.NONE;
        }
        if (normalized.startsWith("/internal/") || normalized.contains("/internal/")) {
            return PlatformApiSecurity.BASIC;
        }
        if (normalized.startsWith("/endpoint/") || normalized.contains("/endpoint/")) {
            return PlatformApiSecurity.BEARER;
        }
        return properties.getDefaultSecurity();
    }
}
