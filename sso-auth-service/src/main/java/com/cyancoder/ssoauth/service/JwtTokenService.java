package com.cyancoder.ssoauth.service;

import com.cyancoder.sso.common.dto.SessionResponse;
import com.cyancoder.sso.common.dto.TokenResponse;
import com.cyancoder.sso.common.dto.UserSummary;
import com.cyancoder.ssoauth.config.JwtConfigurationProperties;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtConfigurationProperties jwtConfigurationProperties;
    private final RefreshTokenService refreshTokenService;

    public JwtTokenService(
            JwtEncoder jwtEncoder,
            JwtConfigurationProperties jwtConfigurationProperties,
            RefreshTokenService refreshTokenService
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtConfigurationProperties = jwtConfigurationProperties;
        this.refreshTokenService = refreshTokenService;
    }

    public TokenResponse issue(String clientId, UserSummary user, SessionResponse sessionResponse) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(jwtConfigurationProperties.getTtlSeconds());

        Map<String, Object> resourceAccess = new HashMap<>();
        resourceAccess.put(jwtConfigurationProperties.getAudience(), Map.of("roles", normalizeRoles(user.roles())));

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer(jwtConfigurationProperties.getIssuer())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(user.username())
                .claim("client_id", clientId)
                .claim("preferred_username", user.username())
                .claim("scope", "openid profile")
                .claim("session_id", sessionResponse.sessionId())
                .claim("realm_access", Map.of("roles", normalizeRoles(user.roles())))
                .claim("resource_access", resourceAccess)
                .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        String tokenValue = jwtEncoder.encode(JwtEncoderParameters.from(header, claimsSet)).getTokenValue();
        String refreshToken = refreshTokenService.issue(clientId, user, sessionResponse, jwtConfigurationProperties.getTtlSeconds());
        return new TokenResponse(tokenValue, refreshToken, "Bearer", jwtConfigurationProperties.getTtlSeconds(), sessionResponse.sessionId());
    }

    private List<String> normalizeRoles(List<String> roles) {
        return roles == null || roles.isEmpty() ? List.of("user") : roles;
    }
}
