package com.cyancoder.ssouser.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.Instant;
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

    // Soft revoke: the row is kept so a withdrawn grant can be restored, and so
    // the audit trail of who granted or revoked it survives the revocation.
    @Column(name = "active", nullable = false, columnDefinition = "boolean not null default true")
    private boolean active = true;

    @Column(name = "granted_at")
    private Instant grantedAt;

    @Column(name = "granted_by")
    private String grantedBy;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revoked_by")
    private String revokedBy;

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

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getGrantedAt() { return grantedAt; }
    public void setGrantedAt(Instant grantedAt) { this.grantedAt = grantedAt; }
    public String getGrantedBy() { return grantedBy; }
    public void setGrantedBy(String grantedBy) { this.grantedBy = grantedBy; }
    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }
    public String getRevokedBy() { return revokedBy; }
    public void setRevokedBy(String revokedBy) { this.revokedBy = revokedBy; }
}
