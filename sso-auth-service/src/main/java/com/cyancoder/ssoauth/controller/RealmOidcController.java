package com.cyancoder.ssoauth.controller;

import com.cyancoder.sso.common.dto.LoginRequest;
import com.cyancoder.sso.common.dto.RefreshTokenRequest;
import com.cyancoder.sso.common.dto.TokenIntrospectionRequest;
import com.cyancoder.sso.common.dto.TokenIntrospectionResponse;
import com.cyancoder.sso.common.dto.TokenResponse;
import com.cyancoder.ssoauth.config.JwtConfigurationProperties;
import com.cyancoder.ssoauth.service.AuthService;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/realms/{realmKey}")
public class RealmOidcController {
    private final AuthService authService;
    private final RSAKey rsaKey;
    private final JwtConfigurationProperties jwtConfigurationProperties;

    public RealmOidcController(AuthService authService, RSAKey rsaKey, JwtConfigurationProperties jwtConfigurationProperties) {
        this.authService = authService;
        this.rsaKey = rsaKey;
        this.jwtConfigurationProperties = jwtConfigurationProperties;
    }

    @GetMapping("/.well-known/openid-configuration")
    public Map<String, Object> openidConfiguration(@PathVariable String realmKey) {
        String issuer = realmIssuer(realmKey);
        return Map.of(
                "issuer", issuer,
                "jwks_uri", issuer + "/protocol/openid-connect/certs",
                "token_endpoint", issuer + "/protocol/openid-connect/token",
                "introspection_endpoint", issuer + "/protocol/openid-connect/token/introspect",
                "end_session_endpoint", issuer + "/protocol/openid-connect/logout"
        );
    }

    @GetMapping("/protocol/openid-connect/certs")
    public Map<String, Object> certs() {
        return new JWKSet(rsaKey.toPublicJWK()).toJSONObject();
    }

    @PostMapping(value = "/protocol/openid-connect/token", consumes = "application/x-www-form-urlencoded")
    public TokenResponse token(@PathVariable String realmKey, @RequestParam MultiValueMap<String, String> form) {
        String grantType = first(form, "grant_type");
        if ("refresh_token".equals(grantType)) {
            return authService.refresh(new RefreshTokenRequest(
                    first(form, "client_id"),
                    first(form, "refresh_token")
            ));
        }
        return authService.login(new LoginRequest(
                first(form, "client_id"),
                first(form, "username"),
                first(form, "password"),
                first(form, "captcha_challenge_id"),
                first(form, "captcha_answer"),
                first(form, "otp_code"),
                first(form, "device_id")
        ));
    }

    @PostMapping(value = "/protocol/openid-connect/token/introspect", consumes = "application/x-www-form-urlencoded")
    public TokenIntrospectionResponse introspect(@RequestParam MultiValueMap<String, String> form) {
        return authService.introspect(new TokenIntrospectionRequest(first(form, "token")));
    }

    @PostMapping(value = "/protocol/openid-connect/logout", consumes = "application/x-www-form-urlencoded")
    public Map<String, Object> logout(@RequestBody(required = false) String ignored) {
        return Map.of("status", "accepted");
    }

    private String realmIssuer(String realmKey) {
        return jwtConfigurationProperties.getIssuer().replaceAll("/+$", "") + "/realms/" + realmKey;
    }

    private String first(MultiValueMap<String, String> form, String key) {
        return form.getFirst(key);
    }
}
