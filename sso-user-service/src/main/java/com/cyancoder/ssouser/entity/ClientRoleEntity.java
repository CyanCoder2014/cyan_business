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
@Table(name = "sso_client_roles")
@IdClass(ClientRoleEntity.ClientRoleId.class)
public class ClientRoleEntity {
    @Id
    @Column(name = "client_id", nullable = false)
    private String clientId;

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
    @CollectionTable(name = "sso_client_role_permissions", joinColumns = {
            @JoinColumn(name = "client_id", referencedColumnName = "client_id"),
            @JoinColumn(name = "role_key", referencedColumnName = "role_key")
    })
    @Column(name = "permission_key")
    private List<String> permissions = new ArrayList<>();

    public static class ClientRoleId implements Serializable {
        private String clientId;
        private String roleKey;

        public ClientRoleId() {
        }

        public ClientRoleId(String clientId, String roleKey) {
            this.clientId = clientId;
            this.roleKey = roleKey;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof ClientRoleId that)) return false;
            return Objects.equals(clientId, that.clientId) && Objects.equals(roleKey, that.roleKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clientId, roleKey);
        }
    }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
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
