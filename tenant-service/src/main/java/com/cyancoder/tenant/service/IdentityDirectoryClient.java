package com.cyancoder.tenant.service;

import com.cyancoder.platform.internalhttp.InternalServiceCredentialsResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class IdentityDirectoryClient {
    public record IdentityUser(String username, String email, String phoneNumber, boolean mfaEnabled, List<String> roles, boolean active) {}
    private final RestClient client;
    private final String authorization;

    public IdentityDirectoryClient(RestClient.Builder builder,
                                   @Value("${sso.user.service.base-url:http://localhost:9002}") String baseUrl,
                                   InternalServiceCredentialsResolver credentialsResolver) {
        client = builder.baseUrl(baseUrl).build();
        authorization = credentialsResolver.authorizationHeader("sso-user-service");
    }

    public IdentityUser get(String username) {
        return client.get().uri("/internal/users/{username}", username).header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve().body(IdentityUser.class);
    }

    public IdentityUser provision(String username, String password, String email, String phoneNumber, boolean mfaRequired) {
        return provision(username, password, email, phoneNumber, mfaRequired, "builder");
    }

    public IdentityUser provision(String username, String password, String email, String phoneNumber, boolean mfaRequired, String clientRole) {
        UserSummary response = client.post().uri("/internal/users/managed").header(HttpHeaders.AUTHORIZATION, authorization)
                .body(new ManagedProvisionRequest(username, password, email, phoneNumber, mfaRequired,
                        "cyan", "cyan-panel", List.of("realm-user"), List.of(clientRole)))
                .retrieve().body(UserSummary.class);
        return new IdentityUser(response.username(), response.email(), response.phoneNumber(), response.mfaEnabled(), response.roles(), response.active());
    }

    /** Null fields are left untouched by the directory. */
    public IdentityUser administer(String username, String email, String phoneNumber, Boolean mfaEnabled, Boolean active) {
        UserSummary response = client.patch().uri("/internal/users/{username}", username)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .body(new AdministerRequest(email, phoneNumber, mfaEnabled, active))
                .retrieve().body(UserSummary.class);
        return new IdentityUser(response.username(), response.email(), response.phoneNumber(), response.mfaEnabled(), response.roles(), response.active());
    }

    public IdentityUser ensurePanelAccess(String username, String clientRole) {
        IdentityUser existing = get(username);
        if (existing == null) throw new IllegalArgumentException("Identity does not exist");
        return provision(existing.username(), null, existing.email(), existing.phoneNumber(), existing.mfaEnabled(), clientRole);
    }

    static String panelRoleForTenantRole(String tenantRole) {
        return switch (tenantRole) {
            case "TENANT_OWNER" -> "client-owner";
            case "TENANT_ADMIN" -> "client-admin";
            default -> "builder";
        };
    }

    private record UserSummary(String username, String email, String phoneNumber, boolean mfaEnabled, List<String> roles, boolean active) {}
    private record AdministerRequest(String email, String phoneNumber, Boolean mfaEnabled, Boolean active) {}
    private record ManagedProvisionRequest(String username, String password, String email, String phoneNumber,
                                           boolean mfaEnabled, String realmKey, String clientId,
                                           List<String> realmRoles, List<String> clientRoles) {}
}
