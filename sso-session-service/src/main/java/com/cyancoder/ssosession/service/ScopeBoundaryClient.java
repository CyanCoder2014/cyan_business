package com.cyancoder.ssosession.service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ScopeBoundaryClient {
    private final RestClient tenant; private final RestClient storefront;
    private final String tenantUser, tenantPassword, storefrontUser, storefrontPassword;
    public ScopeBoundaryClient(RestClient.Builder builder, @Value("${tenant-service.base-url}") String tenantUrl,
            @Value("${tenant-service.internal.username}") String tenantUser, @Value("${tenant-service.internal.password}") String tenantPassword,
            @Value("${storefront-service.base-url}") String storefrontUrl, @Value("${storefront-service.internal.username}") String storefrontUser,
            @Value("${storefront-service.internal.password}") String storefrontPassword) {
        this.tenant = builder.baseUrl(tenantUrl).build(); this.storefront = builder.baseUrl(storefrontUrl).build();
        this.tenantUser = tenantUser; this.tenantPassword = tenantPassword; this.storefrontUser = storefrontUser; this.storefrontPassword = storefrontPassword;
    }
    public void validate(String subject, String tenantKey, String siteKey) {
        try {
            Map<?, ?> membership = tenant.get().uri("/internal/tenants/{tenantKey}/members/{subject}/access", tenantKey, subject)
                    .headers(h -> h.setBasicAuth(tenantUser, tenantPassword, StandardCharsets.UTF_8)).retrieve().body(Map.class);
            if (membership == null || !Boolean.TRUE.equals(membership.get("active"))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tenant membership required");
            if (siteKey != null && !siteKey.isBlank()) {
                Map<?, ?> site = storefront.get().uri(uri -> uri.path("/internal/sites/{siteKey}").queryParam("tenantKey", tenantKey).build(siteKey))
                        .headers(h -> h.setBasicAuth(storefrontUser, storefrontPassword, StandardCharsets.UTF_8)).retrieve().body(Map.class);
                if (site == null || !Boolean.TRUE.equals(site.get("exists"))) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Site does not belong to tenant");
            }
        } catch (ResponseStatusException ex) { throw ex; }
        catch (Exception ex) { throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Scope validation unavailable", ex); }
    }
}
