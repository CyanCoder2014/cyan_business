package com.cyancoder.tenant.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Component
public class IdentityDirectoryClient {
    public record IdentityUser(String username, String email, String phoneNumber, boolean mfaEnabled, List<String> roles, boolean active) {}
    private final RestClient client;
    private final String authorization;

    public IdentityDirectoryClient(RestClient.Builder builder,
                                   @Value("${sso.user.service.base-url:http://localhost:9002}") String baseUrl,
                                   @Value("${sso.user.service.internal.username:sso_user_internal}") String username,
                                   @Value("${sso.user.service.internal.password:sso_user_secret}") String password) {
        client = builder.baseUrl(baseUrl).build();
        authorization = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
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

    private record UserSummary(String username, String email, String phoneNumber, boolean mfaEnabled, List<String> roles, boolean active) {}
    private record ManagedProvisionRequest(String username, String password, String email, String phoneNumber,
                                           boolean mfaEnabled, String realmKey, String clientId,
                                           List<String> realmRoles, List<String> clientRoles) {}
}
