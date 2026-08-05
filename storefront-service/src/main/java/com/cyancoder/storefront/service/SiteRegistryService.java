package com.cyancoder.storefront.service;

import com.cyancoder.storefront.api.SiteContracts.*;
import com.cyancoder.storefront.model.*;
import com.cyancoder.storefront.repository.*;
import java.text.Normalizer;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SiteRegistryService {
    private final SiteRepository sites; private final SiteIdempotencyRepository idempotency; private final TenantMembershipClient memberships;
    public SiteRegistryService(SiteRepository sites, SiteIdempotencyRepository idempotency, TenantMembershipClient memberships) { this.sites = sites; this.idempotency = idempotency; this.memberships = memberships; }
    public List<SiteSummary> list(String tenantKey, String subject) { memberships.requireMembership(tenantKey, subject); return sites.findAllByTenantKeyOrderByNameAsc(tenantKey).stream().map(this::summary).toList(); }
    public SiteMembership internalMembership(String tenantKey, String siteKey) { return new SiteMembership(tenantKey, siteKey, sites.existsByTenantKeyAndSiteKey(tenantKey, siteKey)); }
    @Transactional
    public SiteSummary create(String tenantKey, String subject, String key, CreateSiteRequest request) {
        memberships.requireMembership(tenantKey, subject);
        if (key == null || key.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Idempotency-Key is required");
        var prior = idempotency.findByActorAndIdempotencyKey(subject, key);
        if (prior.isPresent()) return sites.findByTenantKeyAndSiteKey(tenantKey, prior.get().getSiteKey()).map(this::summary).orElseThrow();
        String siteKey = normalize(request.siteKey() == null || request.siteKey().isBlank() ? request.name() : request.siteKey());
        if (sites.existsByTenantKeyAndSiteKey(tenantKey, siteKey)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Site key already exists");
        SiteEntity saved = sites.save(new SiteEntity(tenantKey, siteKey, request.name().trim()));
        idempotency.save(new SiteIdempotencyEntity(subject, key, siteKey));
        return summary(saved);
    }
    private SiteSummary summary(SiteEntity s) { return new SiteSummary(s.getTenantKey(), s.getSiteKey(), s.getName(), s.getStatus(), s.getCreatedAt()); }
    private String normalize(String value) { String key = Normalizer.normalize(value.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFKD).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", ""); if (key.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "siteKey must contain latin letters or digits"); return key; }
}
