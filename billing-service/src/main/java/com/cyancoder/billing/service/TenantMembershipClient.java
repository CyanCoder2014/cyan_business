package com.cyancoder.billing.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class TenantMembershipClient {
    private final RestClient client;
    private final String authorization;
    public TenantMembershipClient(RestClient.Builder builder, @Value("${tenant.service.base-url:http://localhost:9129}") String baseUrl,
                                  @Value("${tenant.service.internal.username:tenant_internal}") String username,
                                  @Value("${tenant.service.internal.password:tenant_secret}") String password) {
        client = builder.baseUrl(baseUrl).build();
        authorization = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
    }
    public void require(String tenantKey, String username) {
        client.get().uri("/internal/tenants/{tenantKey}/members/{username}/access", tenantKey, username)
                .header(HttpHeaders.AUTHORIZATION, authorization).retrieve().toBodilessEntity();
    }
}
