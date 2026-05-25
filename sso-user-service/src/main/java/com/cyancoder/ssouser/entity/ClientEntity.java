package com.cyancoder.ssouser.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sso_clients")
public class ClientEntity {
    @Id
    @Column(name = "client_id", nullable = false, updatable = false)
    private String clientId;

    @Column(name = "realm_key", nullable = false)
    private String realmKey;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "description")
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "public_client", nullable = false)
    private boolean publicClient;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "sso_client_redirect_uris", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "redirect_uri")
    private List<String> redirectUris = new ArrayList<>();

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getRealmKey() { return realmKey; }
    public void setRealmKey(String realmKey) { this.realmKey = realmKey; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isPublicClient() { return publicClient; }
    public void setPublicClient(boolean publicClient) { this.publicClient = publicClient; }
    public List<String> getRedirectUris() { return redirectUris; }
    public void setRedirectUris(List<String> redirectUris) { this.redirectUris = redirectUris; }
}
