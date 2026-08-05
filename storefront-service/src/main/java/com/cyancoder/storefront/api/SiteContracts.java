package com.cyancoder.storefront.api;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public final class SiteContracts {
    private SiteContracts() {}
    public record CreateSiteRequest(@NotBlank String name, String siteKey) {}
    public record SiteSummary(String tenantKey, String siteKey, String name, String status, Instant createdAt) {}
    public record SiteMembership(String tenantKey, String siteKey, boolean exists) {}
}
