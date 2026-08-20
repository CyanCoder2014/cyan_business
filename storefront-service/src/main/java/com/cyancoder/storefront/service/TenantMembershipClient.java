package com.cyancoder.storefront.service;

import com.cyancoder.platform.internalhttp.InternalServiceCredentialsResolver;
import java.util.Map;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Component
public class TenantMembershipClient {
    private final RestClient restClient;
    private final InternalServiceCredentialsResolver credentialsResolver;
    public TenantMembershipClient(RestClient.Builder builder, @Value("${tenant-service.base-url}") String baseUrl,
            InternalServiceCredentialsResolver credentialsResolver) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.credentialsResolver = credentialsResolver;
    }
    public void requireMembership(String tenantKey, String subject) {
        try {
            Map<?, ?> result = restClient.get().uri("/internal/tenants/{tenantKey}/members/{subject}/access", tenantKey, subject)
                    .headers(h -> credentialsResolver.applyBasicAuth(h, "tenant-service")).retrieve().body(Map.class);
            if (result == null || !Boolean.TRUE.equals(result.get("active"))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tenant membership required");
        } catch (ResponseStatusException ex) { throw ex; }
        catch (Exception ex) { throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Tenant membership service unavailable", ex); }
    }
    public void requirePermission(String tenantKey, String subject, String permission) {
        try {
            Map<?, ?> result = restClient.get().uri("/internal/tenants/{tenantKey}/members/{subject}/effective-access", tenantKey, subject)
                    .headers(h -> credentialsResolver.applyBasicAuth(h, "tenant-service")).retrieve().body(Map.class);
            List<?> permissions = result == null || !(result.get("permissions") instanceof List<?> values) ? List.of() : values;
            if (result == null || !Boolean.TRUE.equals(result.get("active")) || (!permissions.contains("*") && !permissions.contains(permission))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Permission is required: " + permission);
            }
        } catch (ResponseStatusException ex) { throw ex; }
        catch (Exception ex) { throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Tenant access service unavailable", ex); }
    }
}
