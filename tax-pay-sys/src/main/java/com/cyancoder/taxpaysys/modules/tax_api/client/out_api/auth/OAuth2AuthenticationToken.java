package com.cyancoder.taxpaysys.modules.tax_api.client.out_api.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.Assert;

import java.util.Collection;

public class OAuth2AuthenticationToken extends AbstractAuthenticationToken {
    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;
    private final OAuth2User principal;
    private final String authorizedClientRegistrationId;

    public OAuth2AuthenticationToken(OAuth2User principal,
                                     Collection authorities,
                                     String authorizedClientRegistrationId) {
        super(authorities);
        Assert.notNull(principal, "principal cannot be null");
        Assert.hasText(authorizedClientRegistrationId, "authorizedClientRegistrationId cannot be empty");
        this.principal = principal;
        this.authorizedClientRegistrationId = authorizedClientRegistrationId;
        this.setAuthenticated(true);
    }

    @Override
    public OAuth2User getPrincipal() {
        return this.principal;
    }

    @Override
    public Object getCredentials() {
        // Credentials are never exposed (by the Provider) for an OAuth2 User
        return "";
    }

    public String getAuthorizedClientRegistrationId() {
        return this.authorizedClientRegistrationId;
    }
}
