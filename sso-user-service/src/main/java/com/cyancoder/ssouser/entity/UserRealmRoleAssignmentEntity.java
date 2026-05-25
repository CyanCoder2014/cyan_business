package com.cyancoder.ssouser.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "sso_user_realm_role_assignments")
@IdClass(UserRealmRoleAssignmentEntity.UserRealmRoleAssignmentId.class)
public class UserRealmRoleAssignmentEntity {
    @Id
    @Column(name = "username", nullable = false)
    private String username;

    @Id
    @Column(name = "realm_key", nullable = false)
    private String realmKey;

    @Id
    @Column(name = "role_key", nullable = false)
    private String roleKey;

    public static class UserRealmRoleAssignmentId implements Serializable {
        private String username;
        private String realmKey;
        private String roleKey;

        public UserRealmRoleAssignmentId() {
        }

        public UserRealmRoleAssignmentId(String username, String realmKey, String roleKey) {
            this.username = username;
            this.realmKey = realmKey;
            this.roleKey = roleKey;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof UserRealmRoleAssignmentId that)) return false;
            return Objects.equals(username, that.username) && Objects.equals(realmKey, that.realmKey) && Objects.equals(roleKey, that.roleKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(username, realmKey, roleKey);
        }
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRealmKey() { return realmKey; }
    public void setRealmKey(String realmKey) { this.realmKey = realmKey; }
    public String getRoleKey() { return roleKey; }
    public void setRoleKey(String roleKey) { this.roleKey = roleKey; }
}
