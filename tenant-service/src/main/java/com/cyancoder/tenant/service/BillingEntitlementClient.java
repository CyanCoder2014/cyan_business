package com.cyancoder.tenant.service;

import com.cyancoder.platform.internalhttp.InternalServiceCredentialsResolver;
import com.cyancoder.tenant.api.TenantContracts.BillingEntitlements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BillingEntitlementClient {
    private final RestClient client;
    private final String authorization;

    public BillingEntitlementClient(
            RestClient.Builder builder,
            @Value("${billing.service.base-url:http://localhost:9130}") String baseUrl,
            InternalServiceCredentialsResolver credentialsResolver
    ) {
        this.client = builder.baseUrl(baseUrl).build();
        this.authorization = credentialsResolver.authorizationHeader("billing-service");
    }

    public BillingEntitlements resolve(String tenantKey) {
        return client.get().uri("/internal/billing/tenants/{tenantKey}/entitlements", tenantKey)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve().body(BillingEntitlements.class);
    }

    public void activateFreePlan(String tenantKey, String planKey, String idempotencyKey) {
        client.post().uri("/internal/billing/tenants/{tenantKey}/subscription/change", tenantKey)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .header("Idempotency-Key", idempotencyKey)
                .body(java.util.Map.of("planKey", planKey)).retrieve().toBodilessEntity();
    }
}
