package com.cyancoder.batchworker.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "batch_definitions",
        uniqueConstraints = @UniqueConstraint(name = "uk_batch_definition_scope_key",
                columnNames = {"tenant_key", "site_key", "definition_key"}),
        indexes = @Index(name = "idx_batch_definition_scope", columnList = "tenant_key,site_key"))
public class BatchDefinition {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_key", nullable = false, length = 120) private String tenantKey;
    @Column(name = "site_key", nullable = false, length = 120) private String siteKey;
    @Column(name = "definition_key", nullable = false, length = 160) private String definitionKey;
    @Column(nullable = false, length = 240) private String title;
    @Lob @Column(name = "spec_json", nullable = false) private String specJson;
    @Column(nullable = false) private boolean active = true;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version private long version;

    public UUID getId() { return id; }
    public String getTenantKey() { return tenantKey; }
    public void setTenantKey(String value) { tenantKey = value; }
    public String getSiteKey() { return siteKey; }
    public void setSiteKey(String value) { siteKey = value; }
    public String getDefinitionKey() { return definitionKey; }
    public void setDefinitionKey(String value) { definitionKey = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { title = value; }
    public String getSpecJson() { return specJson; }
    public void setSpecJson(String value) { specJson = value; }
    public boolean isActive() { return active; }
    public void setActive(boolean value) { active = value; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant value) { createdAt = value; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant value) { updatedAt = value; }
}
