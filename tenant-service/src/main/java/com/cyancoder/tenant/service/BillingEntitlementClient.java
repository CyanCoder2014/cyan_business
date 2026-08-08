package com.cyancoder.tenant.service;

import com.cyancoder.tenant.api.TenantContracts.BillingEntitlements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BillingEntitlementClient {
    private final RestClient client;
    private final String username;
    private final String password;

    public BillingEntitlementClient(
            RestClient.Builder builder,
            @Value("${billing.service.base-url:http://localhost:9130}") String baseUrl,
            @Value("${billing.service.internal.username:billing_internal}") String username,
            @Value("${billing.service.internal.password:billing_secret}") String password
    ) {
        this.client = builder.baseUrl(baseUrl).build();
        this.username = username;
        this.password = password;
    }

    public BillingEntitlements resolve(String tenantKey) {
        return client.get().uri("/internal/billing/tenants/{tenantKey}/entitlements", tenantKey)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + java.util.Base64.getEncoder().encodeToString((username + ":" + password).getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .retrieve().body(BillingEntitlements.class);
    }

    public void activateFreePlan(String tenantKey, String planKey, String idempotencyKey) {
        client.post().uri("/internal/billing/tenants/{tenantKey}/subscription/change", tenantKey)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + java.util.Base64.getEncoder().encodeToString((username + ":" + password).getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .header("Idempotency-Key", idempotencyKey)
                .body(java.util.Map.of("planKey", planKey)).retrieve().toBodilessEntity();
    }
}
