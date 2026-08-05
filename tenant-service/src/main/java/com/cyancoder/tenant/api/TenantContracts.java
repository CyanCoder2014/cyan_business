package com.cyancoder.tenant.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class TenantContracts {
    private TenantContracts() {}

    public record CreateTenantRequest(
            @NotBlank @Pattern(regexp = "[a-z0-9][a-z0-9-]{2,79}") String tenantKey,
            @NotBlank @Size(max = 180) String displayName
    ) {}

    public record TenantSummary(
            String tenantKey,
            String displayName,
            String status,
            String membershipRole,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record EffectiveCapability(
            String key,
            boolean enabled,
            String source,
            String status,
            Map<String, Object> limits,
            String reason
    ) {}

    public record MembershipAccess(String tenantKey, String username, String roleKey, boolean active) {}

    public record BillingEntitlements(String planKey, String status, List<String> features, Map<String, Object> limits) {
        public static BillingEntitlements none() {
            return new BillingEntitlements(null, "NONE", List.of(), Map.of());
        }
    }
}
