package com.cyancoder.billing.service;

import com.cyancoder.platform.internalhttp.InternalServiceCredentialsResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TenantMembershipClient {
    private final RestClient client;
    private final String authorization;
    public TenantMembershipClient(RestClient.Builder builder,
                                  @Value("${tenant.service.base-url:http://localhost:9129}") String baseUrl,
                                  InternalServiceCredentialsResolver credentialsResolver) {
        client = builder.baseUrl(baseUrl).build();
        authorization = credentialsResolver.authorizationHeader("tenant-service");
    }
    public void require(String tenantKey, String username) {
        client.get().uri("/internal/tenants/{tenantKey}/members/{username}/access", tenantKey, username)
                .header(HttpHeaders.AUTHORIZATION, authorization).retrieve().toBodilessEntity();
    }
}
