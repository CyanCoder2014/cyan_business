package com.cyancoder.platform.internalhttp;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class InternalServiceCredentialsResolverTest {
    @Test
    void resolvesCanonicalTargetServiceEnvironmentProperties() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("CONTENT_SERVICE_INTERNAL_USERNAME", "runtime-content-user")
                .withProperty("CONTENT_SERVICE_INTERNAL_PASSWORD", "runtime-content-password")
                .withProperty("content-service.internal.username", "legacy-content-user")
                .withProperty("content-service.internal.password", "legacy-content-password");

        InternalServiceCredentials credentials =
                new InternalServiceCredentialsResolver(environment).resolve("content-service");

        assertThat(credentials.username()).isEqualTo("runtime-content-user");
        assertThat(credentials.password()).isEqualTo("runtime-content-password");
    }

    @Test
    void keepsServiceSuffixInCanonicalVariableName() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("CATALOG_SERVICE_INTERNAL_USERNAME", "catalog-runtime")
                .withProperty("CATALOG_SERVICE_INTERNAL_PASSWORD", "catalog-password")
                .withProperty("CRM_SERVICE_INTERNAL_USERNAME", "crm-runtime")
                .withProperty("CRM_SERVICE_INTERNAL_PASSWORD", "crm-password");
        InternalServiceCredentialsResolver resolver = new InternalServiceCredentialsResolver(environment);

        assertThat(resolver.resolve("catalog-service"))
                .isEqualTo(new InternalServiceCredentials("catalog-runtime", "catalog-password"));
        assertThat(resolver.resolve("crm-service"))
                .isEqualTo(new InternalServiceCredentials("crm-runtime", "crm-password"));
    }

    @Test
    void resolvesLegacyTargetPropertySpellings() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("sso.user.service.internal.username", "runtime-sso-user")
                .withProperty("sso.user.service.internal.password", "runtime-sso-password");

        InternalServiceCredentials credentials =
                new InternalServiceCredentialsResolver(environment).resolve("sso-user-service");

        assertThat(credentials.username()).isEqualTo("runtime-sso-user");
        assertThat(credentials.password()).isEqualTo("runtime-sso-password");
    }

    @Test
    void retainsDerivedCredentialsOnlyAsFallbacks() {
        InternalServiceCredentials credentials =
                new InternalServiceCredentialsResolver(new MockEnvironment()).resolve("pricing-promotion-service");

        assertThat(credentials.username()).isEqualTo("pricing_promotion_internal");
        assertThat(credentials.password()).isEqualTo("pricing_promotion_secret");
    }
}
