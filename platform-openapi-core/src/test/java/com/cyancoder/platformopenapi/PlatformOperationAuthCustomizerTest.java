package com.cyancoder.platformopenapi;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformOperationAuthCustomizerTest {

    @Test
    void methodAuthenticationOverridesControllerAuthentication() throws Exception {
        HandlerMethod publicMethod = new HandlerMethod(
                new MixedAuthenticationController(),
                MixedAuthenticationController.class.getMethod("login"));
        HandlerMethod securedMethod = new HandlerMethod(
                new MixedAuthenticationController(),
                MixedAuthenticationController.class.getMethod("logout"));
        PlatformOperationAuthCustomizer customizer = new PlatformOperationAuthCustomizer();

        Operation login = customizer.customize(new Operation(), publicMethod);
        Operation logout = customizer.customize(new Operation(), securedMethod);
        OpenAPI openApi = new OpenAPI().paths(new Paths()
                .addPathItem("/api/sso/auth/login", new PathItem().post(login))
                .addPathItem("/api/sso/auth/logout", new PathItem().post(logout)));
        new PlatformOpenApiCustomizer(new PlatformOpenApiProperties()).customise(openApi);

        assertThat(login.getExtensions()).containsEntry("x-platform-auth", "NONE");
        assertThat(login.getSecurity()).isEmpty();
        assertThat(logout.getExtensions()).containsEntry("x-platform-auth", "BEARER");
        assertThat(logout.getSecurity().get(0)).containsKey("bearerAuth");
    }

    @PlatformOpenApiAuth(PlatformApiSecurity.NONE)
    static class MixedAuthenticationController {
        public void login() {
        }

        @PlatformOpenApiAuth(PlatformApiSecurity.BEARER)
        public void logout() {
        }
    }
}
