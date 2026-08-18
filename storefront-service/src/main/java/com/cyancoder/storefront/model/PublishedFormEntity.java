package com.cyancoder.storefront.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "published_forms")
public class PublishedFormEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 120) private String slug;
    @Column(name = "tenant_key", nullable = false, length = 120) private String tenantKey;
    @Column(name = "site_key", nullable = false, length = 120) private String siteKey = "";
    @Column(name = "service_key", nullable = false, length = 120) private String serviceKey;
    @Column(name = "entity_key", nullable = false, length = 180) private String entityKey;
    @Column(nullable = false, length = 240) private String title;
    @Column(length = 1000) private String description;
    @Column(nullable = false, length = 24) private String visibility;
    @Column(nullable = false, length = 24) private String status;
    @Column(name = "created_by", nullable = false, length = 180) private String createdBy;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected PublishedFormEntity() {}

    public PublishedFormEntity(String slug, String tenantKey, String siteKey, String serviceKey, String entityKey,
                               String title, String description, String visibility, String createdBy) {
        this.slug = slug; this.tenantKey = tenantKey; this.siteKey = siteKey == null ? "" : siteKey;
        this.serviceKey = serviceKey; this.entityKey = entityKey; this.title = title;
        this.description = description; this.visibility = visibility; this.status = "PUBLISHED";
        this.createdBy = createdBy; this.createdAt = Instant.now(); this.updatedAt = this.createdAt;
    }

    public Long getId() { return id; }
    public String getSlug() { return slug; }
    public String getTenantKey() { return tenantKey; }
    public String getSiteKey() { return siteKey; }
    public String getServiceKey() { return serviceKey; }
    public String getEntityKey() { return entityKey; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getVisibility() { return visibility; }
    public String getStatus() { return status; }
    public String getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void republish(String title, String description, String visibility) { this.title = title; this.description = description; this.visibility = visibility; this.status = "PUBLISHED"; this.updatedAt = Instant.now(); }
    public void archive() { this.status = "ARCHIVED"; this.updatedAt = Instant.now(); }
}
