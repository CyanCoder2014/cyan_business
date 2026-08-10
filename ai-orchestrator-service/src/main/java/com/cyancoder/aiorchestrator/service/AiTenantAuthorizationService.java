package com.cyancoder.aiorchestrator.service;

import com.cyancoder.aiorchestrator.client.impl.InternalServiceHttpSupport;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/** Uses Tenant's persisted effective access instead of trusting browser role claims. */
@Service
public class AiTenantAuthorizationService {
    private final InternalServiceHttpSupport http;
    private final ObjectMapper json;

    public AiTenantAuthorizationService(InternalServiceHttpSupport http, ObjectMapper json) {
        this.http = http;
        this.json = json;
    }

    public void requireRead(String tenantKey, String siteKey, String username) {
        require(tenantKey, siteKey, username, "ai.read");
    }

    public void requireExecute(String tenantKey, String siteKey, String username) {
        require(tenantKey, siteKey, username, "ai.execute");
    }

    private void require(String tenantKey, String siteKey, String username, String permission) {
        if (tenantKey == null || tenantKey.isBlank() || username == null || username.isBlank()) {
            throw new AccessDeniedException("AI_PERMISSION_DENIED");
        }
        Map<String, Object> access = readMap(http.get(
                "tenant-service",
                "/internal/tenants/" + encode(tenantKey) + "/members/" + encode(username) + "/effective-access",
                tenantKey,
                siteKey));
        Collection<?> permissions = access.get("permissions") instanceof Collection<?> values ? values : List.of();
        if (!permissions.contains("*") && !permissions.contains(permission)) {
            throw new AccessDeniedException("AI_PERMISSION_DENIED");
        }
        List<Map<String, Object>> capabilities = readList(http.get(
                "tenant-service",
                "/internal/tenants/" + encode(tenantKey) + "/capabilities"
                        + (siteKey == null || siteKey.isBlank() ? "" : "?siteKey=" + encode(siteKey)),
                tenantKey,
                siteKey));
        boolean available = capabilities.stream().anyMatch(item ->
                "ai-orchestrator".equals(item.get("key"))
                        && Boolean.TRUE.equals(item.get("enabled"))
                        && "AVAILABLE".equals(Objects.toString(item.get("status"), "")));
        if (!available) {
            throw new AccessDeniedException("AI_CAPABILITY_DISABLED");
        }
    }

    private Map<String, Object> readMap(String value) {
        try {
            return json.readValue(value, new TypeReference<>() {});
        } catch (Exception exception) {
            throw new IllegalStateException("Tenant effective-access response is invalid", exception);
        }
    }

    private List<Map<String, Object>> readList(String value) {
        try {
            return json.readValue(value, new TypeReference<>() {});
        } catch (Exception exception) {
            throw new IllegalStateException("Tenant capability response is invalid", exception);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
