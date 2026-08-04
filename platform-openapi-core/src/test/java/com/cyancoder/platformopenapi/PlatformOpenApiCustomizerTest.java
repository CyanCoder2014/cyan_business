package com.cyancoder.platformopenapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformOpenApiCustomizerTest {

    @Test
    void derivesOperationAuthenticationFromControllerPathConventions() {
        PlatformOpenApiProperties properties = new PlatformOpenApiProperties();
        OpenAPI openApi = new OpenAPI().paths(new Paths()
                .addPathItem("/internal/entities/records/order", getPath())
                .addPathItem("/endpoint/entities/records/order", getPath())
                .addPathItem("/api/commerce-service/endpoint/entities/records/order", getPath())
                .addPathItem("/public/storefront/routes/home", getPath())
                .addPathItem("/api/processor-service/processors", getPath()));

        new PlatformOpenApiCustomizer(properties).customise(openApi);

        assertSecurity(openApi, "/internal/entities/records/order", "basicAuth", "BASIC");
        assertSecurity(openApi, "/endpoint/entities/records/order", "bearerAuth", "BEARER");
        assertSecurity(
                openApi,
                "/api/commerce-service/endpoint/entities/records/order",
                "bearerAuth",
                "BEARER");
        Operation publicOperation = operation(openApi, "/public/storefront/routes/home");
        assertThat(publicOperation.getSecurity()).isEmpty();
        assertThat(publicOperation.getExtensions()).containsEntry("x-platform-auth", "NONE");
        assertSecurity(
                openApi,
                "/api/processor-service/processors",
                "bearerAuth",
                "BEARER");
        assertThat(openApi.getComponents().getSecuritySchemes())
                .containsKeys("basicAuth", "bearerAuth");
    }

    private PathItem getPath() {
        return new PathItem().get(new Operation());
    }

    private void assertSecurity(
            OpenAPI openApi,
            String path,
            String scheme,
            String extension
    ) {
        Operation operation = operation(openApi, path);
        assertThat(operation.getSecurity()).hasSize(1);
        assertThat(operation.getSecurity().get(0)).containsKey(scheme);
        assertThat(operation.getExtensions()).containsEntry("x-platform-auth", extension);
    }

    private Operation operation(OpenAPI openApi, String path) {
        return openApi.getPaths().get(path).getGet();
    }
}
