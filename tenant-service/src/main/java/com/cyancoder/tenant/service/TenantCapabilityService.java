package com.cyancoder.tenant.service;

import com.cyancoder.tenant.api.TenantContracts.BillingEntitlements;
import com.cyancoder.tenant.api.TenantContracts.EffectiveCapability;
import com.cyancoder.tenant.model.TenantCapabilityOverrideEntity;
import com.cyancoder.tenant.repository.TenantCapabilityOverrideRepository;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TenantCapabilityService {
    private static final Map<String, List<String>> CAPABILITY_SERVICES = Map.ofEntries(
            Map.entry("ai-orchestrator", List.of("ai-orchestrator-service")),
            Map.entry("dynamic-entities", List.of("content-service", "catalog-service", "crm-service")),
            Map.entry("automation", List.of("automation-orchestrator-service")),
            Map.entry("bpm", List.of("bpm-service")),
            Map.entry("processor", List.of("processor-service")),
            Map.entry("notification", List.of("notification-service")),
            Map.entry("report", List.of("report-service")),
            Map.entry("media", List.of("media-service")),
            Map.entry("search", List.of("search-index-service")),
            Map.entry("site-builder", List.of("storefront-service")),
            Map.entry("bot-adapter", List.of("bot-adapter-service")),
            Map.entry("commerce", List.of("commerce-service", "cart-service", "checkout-service")),
            Map.entry("payment", List.of("payment-service", "payment-orchestrator-service"))
    );

    private final TenantCapabilityOverrideRepository overrideRepository;
    private final BillingEntitlementClient billingClient;
    private final DiscoveryClient discoveryClient;

    public TenantCapabilityService(TenantCapabilityOverrideRepository overrideRepository, BillingEntitlementClient billingClient, DiscoveryClient discoveryClient) {
        this.overrideRepository = overrideRepository;
        this.billingClient = billingClient;
        this.discoveryClient = discoveryClient;
    }

    public List<EffectiveCapability> resolve(String tenantKey, String siteKey) {
        BillingEntitlements entitlements;
        String billingFailure = null;
        try {
            entitlements = billingClient.resolve(tenantKey);
            if (entitlements == null) entitlements = BillingEntitlements.none();
        } catch (RuntimeException error) {
            entitlements = BillingEntitlements.none();
            billingFailure = "Billing entitlement service is unavailable";
        }

        Map<String, TenantCapabilityOverrideEntity> overrides = new LinkedHashMap<>();
        for (TenantCapabilityOverrideEntity item : overrideRepository.findByTenantKey(tenantKey)) {
            if (item.getSiteKey() == null || item.getSiteKey().isBlank() || item.getSiteKey().equals(siteKey)) {
                overrides.put(item.getCapabilityKey(), item);
            }
        }

        BillingEntitlements resolved = entitlements;
        String failure = billingFailure;
        return CAPABILITY_SERVICES.entrySet().stream().map(entry -> {
            TenantCapabilityOverrideEntity override = overrides.get(entry.getKey());
            boolean enabled = override != null ? override.isEnabled() : resolved.features().contains(entry.getKey());
            long healthyServices = entry.getValue().stream().filter(service -> !discoveryClient.getInstances(service).isEmpty()).count();
            boolean healthy = healthyServices == entry.getValue().size();
            String status = healthy ? "AVAILABLE" : healthyServices == 0 ? "UNAVAILABLE" : "DEGRADED";
            String reason = override != null ? override.getReason() : failure;
            if (!healthy && (reason == null || reason.isBlank())) reason = "Required service is not registered";
            return new EffectiveCapability(
                    entry.getKey(), enabled, override == null ? "PLAN" : "TENANT_OVERRIDE", status,
                    resolved.limits() == null ? Map.of() : resolved.limits(), reason
            );
        }).toList();
    }
}
