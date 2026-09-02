package com.cyancoder.ssoauth.controller;

import com.cyancoder.platformopenapi.PlatformApiSecurity;
import com.cyancoder.platformopenapi.PlatformOpenApiAuth;
import com.cyancoder.sso.common.dto.*;
import com.cyancoder.ssoauth.config.JwtConfigurationProperties;
import com.cyancoder.ssoauth.service.AuthService;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping
@PlatformOpenApiAuth(PlatformApiSecurity.NONE)
public class AuthController {

    private final AuthService authService;
    private final RSAKey rsaKey;
    private final JwtConfigurationProperties jwtConfigurationProperties;

    public AuthController(AuthService authService, RSAKey rsaKey, JwtConfigurationProperties jwtConfigurationProperties) {
        this.authService = authService;
        this.rsaKey = rsaKey;
        this.jwtConfigurationProperties = jwtConfigurationProperties;
    }

    @PostMapping("/api/sso/auth/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/api/sso/auth/logout")
    public SessionResponse logout(@RequestBody LogoutRequest request) {
        return authService.logout(request);
    }

    @PostMapping("/api/sso/auth/refresh")
    public TokenResponse refresh(@RequestBody RefreshTokenRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/api/sso/auth/introspect")
    public TokenIntrospectionResponse introspect(@RequestBody TokenIntrospectionRequest request) {
        return authService.introspect(request);
    }

    @PostMapping("/api/sso/auth/otp/send")
    public OtpSendResponse sendOtp(
            @RequestBody OtpSendRequest request,
            @RequestParam String captchaChallengeId,
            @RequestParam String captchaAnswer
    ) {
        return authService.sendLoginOtp(request, captchaChallengeId, captchaAnswer);
    }

    public record PasswordResetRequest(String username, String clientId, String language) {}
    public record PasswordResetConfirmRequest(String username, String clientId, String code, String newPassword) {}

    /** Always 204, whether or not the account exists — see AuthService. */
    @PostMapping("/api/sso/auth/password/reset/request")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestPasswordReset(
            @RequestBody PasswordResetRequest request,
            @RequestParam String captchaChallengeId,
            @RequestParam String captchaAnswer
    ) {
        authService.requestPasswordReset(request.username(), request.clientId(), captchaChallengeId, captchaAnswer, request.language());
    }

    @PostMapping("/api/sso/auth/password/reset/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmPasswordReset(@RequestBody PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request.username(), request.clientId(), request.code(), request.newPassword());
    }

    @PostMapping("/api/sso/auth/fido/challenge")
    public FidoChallengeResponse challenge(@RequestBody FidoChallengeRequest request) {
        return authService.startFido(request);
    }

    @PostMapping("/api/sso/auth/fido/verify")
    public FidoVerifyResponse verify(@RequestBody FidoVerifyRequest request) {
        return authService.verifyFido(request);
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        return new JWKSet(rsaKey.toPublicJWK()).toJSONObject();
    }

    @GetMapping("/.well-known/openid-configuration")
    public Map<String, Object> openIdConfiguration() {
        String issuer = jwtConfigurationProperties.getIssuer();
        return Map.of(
                "issuer", issuer,
                "jwks_uri", issuer + "/.well-known/jwks.json",
                "token_endpoint", issuer + "/api/sso/auth/login",
                "revocation_endpoint", issuer + "/api/sso/auth/logout",
                "introspection_endpoint", issuer + "/api/sso/auth/introspect"
        );
    }
}
