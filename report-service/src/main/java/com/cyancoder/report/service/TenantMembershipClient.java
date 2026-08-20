package com.cyancoder.report.service;

import com.cyancoder.platform.internalhttp.InternalServiceCredentialsResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Component
public class TenantMembershipClient {
    private final RestClient client;
    private final InternalServiceCredentialsResolver credentialsResolver;

    public TenantMembershipClient(RestClient.Builder builder,
                                  @Value("${tenant-service.base-url:http://localhost:9129}") String url,
                                  InternalServiceCredentialsResolver credentialsResolver) {
        this.client = builder.baseUrl(url).build();
        this.credentialsResolver = credentialsResolver;
    }

    public void requireMembership(String tenant, String subject) {
        if (tenant == null || tenant.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "X-Tenant-Key is required");
        }
        try {
            Map<?, ?> result = client.get()
                    .uri("/internal/tenants/{tenant}/members/{subject}/access", tenant, subject)
                    .headers(headers -> credentialsResolver.applyBasicAuth(headers, "tenant-service"))
                    .retrieve()
                    .body(Map.class);
            if (result == null || !Boolean.TRUE.equals(result.get("active"))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tenant membership required");
            }
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Tenant membership service unavailable", exception);
        }
    }
}
