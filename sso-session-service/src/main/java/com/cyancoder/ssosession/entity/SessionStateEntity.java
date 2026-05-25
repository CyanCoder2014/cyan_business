package com.cyancoder.ssosession.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sso_sessions")
public class SessionStateEntity {

    @Id
    @Column(name = "session_id", nullable = false, updatable = false)
    private String sessionId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "issued_at_epoch_second", nullable = false)
    private long issuedAtEpochSecond;

    @Column(name = "expires_at_epoch_second", nullable = false)
    private long expiresAtEpochSecond;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getIssuedAtEpochSecond() {
        return issuedAtEpochSecond;
    }

    public void setIssuedAtEpochSecond(long issuedAtEpochSecond) {
        this.issuedAtEpochSecond = issuedAtEpochSecond;
    }

    public long getExpiresAtEpochSecond() {
        return expiresAtEpochSecond;
    }

    public void setExpiresAtEpochSecond(long expiresAtEpochSecond) {
        this.expiresAtEpochSecond = expiresAtEpochSecond;
    }
}
