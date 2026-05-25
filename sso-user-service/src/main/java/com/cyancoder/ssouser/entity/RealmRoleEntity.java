package com.cyancoder.ssouser.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sso_realm_roles")
@IdClass(RealmRoleEntity.RealmRoleId.class)
public class RealmRoleEntity {
    @Id
    @Column(name = "realm_key", nullable = false)
    private String realmKey;

    @Id
    @Column(name = "role_key", nullable = false)
    private String roleKey;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "description")
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "sso_realm_role_permissions", joinColumns = {
            @JoinColumn(name = "realm_key", referencedColumnName = "realm_key"),
            @JoinColumn(name = "role_key", referencedColumnName = "role_key")
    })
    @Column(name = "permission_key")
    private List<String> permissions = new ArrayList<>();

    public static class RealmRoleId implements Serializable {
        private String realmKey;
        private String roleKey;

        public RealmRoleId() {
        }

        public RealmRoleId(String realmKey, String roleKey) {
            this.realmKey = realmKey;
            this.roleKey = roleKey;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof RealmRoleId that)) return false;
            return Objects.equals(realmKey, that.realmKey) && Objects.equals(roleKey, that.roleKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(realmKey, roleKey);
        }
    }

    public String getRealmKey() { return realmKey; }
    public void setRealmKey(String realmKey) { this.realmKey = realmKey; }
    public String getRoleKey() { return roleKey; }
    public void setRoleKey(String roleKey) { this.roleKey = roleKey; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}
