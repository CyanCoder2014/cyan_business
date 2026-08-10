package com.cyancoder.automationorchestrator.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/** Revalidates tenant-effective AI access instead of trusting browser-supplied roles. */
@Service
public class AutomationAiAuthorizationService {
    private final InternalServiceHttpSupport http;

    public AutomationAiAuthorizationService(InternalServiceHttpSupport http) { this.http = http; }

    public void requireBuilder(String tenantKey, String siteKey, String username) {
        require(tenantKey, siteKey, username, "automation.manage");
    }

    public void requireExecution(String tenantKey, String siteKey, String username) {
        require(tenantKey, siteKey, username, "automation.execute");
    }

    private void require(String tenantKey, String siteKey, String username, String automationPermission) {
        if (tenantKey == null || tenantKey.isBlank()) throw new IllegalArgumentException("X-Tenant-Key is required for AI automation");
        if (username == null || username.isBlank()) throw new AccessDeniedException("AI_PERMISSION_DENIED");
        Map<?,?> access = http.exchange("tenant-service",
                "/internal/tenants/" + encode(tenantKey) + "/members/" + encode(username) + "/effective-access",
                HttpMethod.GET, null, http.internalHeaders("tenant-service", tenantKey, siteKey), Map.class);
        Collection<?> permissions = access == null || !(access.get("permissions") instanceof Collection<?> values) ? List.of() : values;
        if (!permissions.contains("*") && (!permissions.contains(automationPermission) || !permissions.contains("ai.execute"))) {
            throw new AccessDeniedException("AI_PERMISSION_DENIED");
        }
        List<?> capabilities = http.exchange("tenant-service",
                "/internal/tenants/" + encode(tenantKey) + "/capabilities" + (siteKey == null || siteKey.isBlank() ? "" : "?siteKey=" + encode(siteKey)),
                HttpMethod.GET, null, http.internalHeaders("tenant-service", tenantKey, siteKey), List.class);
        boolean available = capabilities != null && capabilities.stream().filter(Map.class::isInstance).map(Map.class::cast)
                .anyMatch(item -> "ai-orchestrator".equals(item.get("key")) && Boolean.TRUE.equals(item.get("enabled")) && "AVAILABLE".equals(Objects.toString(item.get("status"), "")));
        if (!available) throw new AccessDeniedException("AI_CAPABILITY_DISABLED");
    }

    private String encode(String value) { return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8); }
}
