package com.cyancoder.storefront.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "site_idempotency", uniqueConstraints = @UniqueConstraint(name = "uk_site_idempotency", columnNames = {"actor", "idempotency_key"}))
public class SiteIdempotencyEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 180) private String actor;
    @Column(name = "idempotency_key", nullable = false, length = 180) private String idempotencyKey;
    @Column(name = "site_key", nullable = false, length = 120) private String siteKey;
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
    protected SiteIdempotencyEntity() {}
    public SiteIdempotencyEntity(String actor, String idempotencyKey, String siteKey) { this.actor = actor; this.idempotencyKey = idempotencyKey; this.siteKey = siteKey; }
    public String getSiteKey() { return siteKey; }
}
