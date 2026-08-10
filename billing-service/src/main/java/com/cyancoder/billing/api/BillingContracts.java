package com.cyancoder.billing.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class BillingContracts {
    private BillingContracts() {}
    public record PlanUpsertRequest(
            @NotBlank @Pattern(regexp = "[a-z0-9][a-z0-9-]{1,79}") String planKey,
            @NotBlank @Size(max = 180) String displayName,
            @Size(max = 1000) String description,
            @NotBlank @Pattern(regexp = "FREE|EXTERNAL") String billingMode,
            boolean active,
            List<String> features,
            Map<String, Object> limits
    ) {}
    public record PlanSummary(String planKey, String displayName, String description, String billingMode, boolean active, List<String> features, Map<String, Object> limits) {}
    public record SubscriptionSummary(String tenantKey, String planKey, String status, Instant startedAt, Instant renewsAt, List<String> features, Map<String, Object> limits, String providerState) {
        public static SubscriptionSummary none(String tenantKey) { return new SubscriptionSummary(tenantKey, null, "NONE", null, null, List.of(), Map.of(), "NOT_CONFIGURED"); }
    }
    public record ChangeSubscriptionRequest(@NotBlank String planKey) {}
    public record BillingEntitlements(String planKey, String status, List<String> features, Map<String, Object> limits) {}
}
