package com.cyancoder.storefront.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "sites", uniqueConstraints = @UniqueConstraint(name = "uk_site_tenant_key", columnNames = {"tenant_key", "site_key"}))
public class SiteEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "tenant_key", nullable = false, length = 120) private String tenantKey;
    @Column(name = "site_key", nullable = false, length = 120) private String siteKey;
    @Column(nullable = false, length = 200) private String name;
    @Column(nullable = false, length = 24) private String status = "ACTIVE";
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
    protected SiteEntity() {}
    public SiteEntity(String tenantKey, String siteKey, String name) { this.tenantKey = tenantKey; this.siteKey = siteKey; this.name = name; }
    public String getTenantKey() { return tenantKey; }
    public String getSiteKey() { return siteKey; }
    public String getName() { return name; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
