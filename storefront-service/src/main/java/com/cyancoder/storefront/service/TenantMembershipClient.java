package com.cyancoder.storefront.service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Component
public class TenantMembershipClient {
    private final RestClient restClient;
    private final String username;
    private final String password;
    public TenantMembershipClient(RestClient.Builder builder, @Value("${tenant-service.base-url}") String baseUrl,
            @Value("${tenant-service.internal.username}") String username, @Value("${tenant-service.internal.password}") String password) {
        this.restClient = builder.baseUrl(baseUrl).build(); this.username = username; this.password = password;
    }
    public void requireMembership(String tenantKey, String subject) {
        try {
            Map<?, ?> result = restClient.get().uri("/internal/tenants/{tenantKey}/members/{subject}/access", tenantKey, subject)
                    .headers(h -> h.setBasicAuth(username, password, StandardCharsets.UTF_8)).retrieve().body(Map.class);
            if (result == null || !Boolean.TRUE.equals(result.get("active"))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tenant membership required");
        } catch (ResponseStatusException ex) { throw ex; }
        catch (Exception ex) { throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Tenant membership service unavailable", ex); }
    }
}
