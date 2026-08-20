package com.cyancoder.tenant.service;

import com.cyancoder.platform.internalhttp.InternalServiceCredentialsResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@Component
public class NotificationInvitationClient {
    private final RestClient client;
    private final String authorization;
    private final String acceptBaseUrl;

    public NotificationInvitationClient(
            RestClient.Builder builder,
            @Value("${notification.service.base-url:http://localhost:9122}") String baseUrl,
            @Value("${tenant.invitation.accept-base-url:http://localhost:3000/auth/invitation}") String acceptBaseUrl,
            InternalServiceCredentialsResolver credentialsResolver) {
        this.client = builder.baseUrl(baseUrl).build();
        this.authorization = credentialsResolver.authorizationHeader("notification-service");
        this.acceptBaseUrl = acceptBaseUrl;
    }

    public String send(String tenant, String invitationId, String email, String rawToken, Instant expires) {
        try {
            Map<?, ?> response = client.post()
                    .uri("/internal/notifications/send")
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .body(Map.of(
                            "messageKey", "tenant-invitation-" + invitationId,
                            "channel", "EMAIL",
                            "provider", "EMAIL",
                            "dispatchMode", "SYNC",
                            "recipient", email,
                            "subject", "Workspace invitation",
                            "body", "You were invited to a Cyan workspace. Accept before " + expires + ": "
                                    + acceptBaseUrl + "?token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8),
                            "model", Map.of("tenantKey", tenant, "expiresAt", expires.toString()),
                            "relatedRef", Map.of("service", "tenant-service", "invitationId", invitationId)))
                    .retrieve()
                    .body(Map.class);
            return response == null ? "NOT_CONFIGURED" : Objects.toString(response.get("status"), "NOT_CONFIGURED");
        } catch (RuntimeException exception) {
            return "NOT_CONFIGURED";
        }
    }
}
