package com.cyancoder.tenant.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "tenant_roles", uniqueConstraints = @UniqueConstraint(name = "uq_tenant_role", columnNames = {"tenant_key", "role_key"}))
public class TenantRoleEntity {
    @Id @Column(name = "role_id", nullable = false, updatable = false, length = 180)
    private String roleId;
    @Column(name = "tenant_key", nullable = false, length = 80)
    private String tenantKey;
    @Column(name = "role_key", nullable = false, length = 64)
    private String roleKey;
    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;
    @Column(name = "description", length = 400)
    private String description;
    @Column(name = "system_role", nullable = false)
    private boolean systemRole;
    @Version @Column(name = "revision", nullable = false)
    private long revision;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tenant_role_permissions", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "permission_key", nullable = false, length = 120)
    private Set<String> permissions = new LinkedHashSet<>();
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public String getRoleId() { return roleId; }
    public void setRoleId(String value) { roleId = value; }
    public String getTenantKey() { return tenantKey; }
    public void setTenantKey(String value) { tenantKey = value; }
    public String getRoleKey() { return roleKey; }
    public void setRoleKey(String value) { roleKey = value; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String value) { displayName = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
    public boolean isSystemRole() { return systemRole; }
    public void setSystemRole(boolean value) { systemRole = value; }
    public long getRevision() { return revision; }
    public Set<String> getPermissions() { return permissions; }
    public void setPermissions(Set<String> value) { permissions = value; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant value) { createdAt = value; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant value) { updatedAt = value; }
}
