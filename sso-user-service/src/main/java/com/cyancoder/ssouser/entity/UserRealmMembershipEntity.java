package com.cyancoder.ssouser.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "sso_user_realm_memberships")
@IdClass(UserRealmMembershipEntity.UserRealmMembershipId.class)
public class UserRealmMembershipEntity {
    @Id
    @Column(name = "username", nullable = false)
    private String username;

    @Id
    @Column(name = "realm_key", nullable = false)
    private String realmKey;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "default_realm", nullable = false)
    private boolean defaultRealm;

    public static class UserRealmMembershipId implements Serializable {
        private String username;
        private String realmKey;

        public UserRealmMembershipId() {
        }

        public UserRealmMembershipId(String username, String realmKey) {
            this.username = username;
            this.realmKey = realmKey;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof UserRealmMembershipId that)) return false;
            return Objects.equals(username, that.username) && Objects.equals(realmKey, that.realmKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(username, realmKey);
        }
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRealmKey() { return realmKey; }
    public void setRealmKey(String realmKey) { this.realmKey = realmKey; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isDefaultRealm() { return defaultRealm; }
    public void setDefaultRealm(boolean defaultRealm) { this.defaultRealm = defaultRealm; }
}
