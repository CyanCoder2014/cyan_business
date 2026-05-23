package com.cyancoder.ssouser.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "sso_user_client_role_assignments")
@IdClass(UserClientRoleAssignmentEntity.UserClientRoleAssignmentId.class)
public class UserClientRoleAssignmentEntity {
    @Id
    @Column(name = "username", nullable = false)
    private String username;

    @Id
    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Id
    @Column(name = "role_key", nullable = false)
    private String roleKey;

    public static class UserClientRoleAssignmentId implements Serializable {
        private String username;
        private String clientId;
        private String roleKey;

        public UserClientRoleAssignmentId() {
        }

        public UserClientRoleAssignmentId(String username, String clientId, String roleKey) {
            this.username = username;
            this.clientId = clientId;
            this.roleKey = roleKey;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof UserClientRoleAssignmentId that)) return false;
            return Objects.equals(username, that.username) && Objects.equals(clientId, that.clientId) && Objects.equals(roleKey, that.roleKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(username, clientId, roleKey);
        }
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getRoleKey() { return roleKey; }
    public void setRoleKey(String roleKey) { this.roleKey = roleKey; }
}
