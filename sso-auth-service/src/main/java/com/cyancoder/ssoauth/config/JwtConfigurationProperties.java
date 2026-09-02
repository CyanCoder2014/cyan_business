package com.cyancoder.ssoauth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sso.jwt")
public class JwtConfigurationProperties {
    private String issuer;
    private String audience;
    private long ttlSeconds;
    /**
     * How long a refresh token lives, i.e. how long a signed-in user stays
     * signed in. Kept independent of ttlSeconds so the access token can be made
     * short-lived — which bounds how long a stolen one outlives a password
     * reset — without also shortening the session.
     */
    private long refreshTtlSeconds = 86400;

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    public long getRefreshTtlSeconds() {
        return refreshTtlSeconds;
    }

    public void setRefreshTtlSeconds(long refreshTtlSeconds) {
        this.refreshTtlSeconds = refreshTtlSeconds;
    }
}
