package com.cyancoder.ssoauth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sso_refresh_tokens")
public class RefreshTokenEntity {

    @Id
    @Column(name = "token", nullable = false, length = 256)
    private String token;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "expires_at_epoch_second", nullable = false)
    private long expiresAtEpochSecond;

    @Column(name = "active", nullable = false)
    private boolean active;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getExpiresAtEpochSecond() {
        return expiresAtEpochSecond;
    }

    public void setExpiresAtEpochSecond(long expiresAtEpochSecond) {
        this.expiresAtEpochSecond = expiresAtEpochSecond;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
