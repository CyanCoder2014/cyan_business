package com.cyancoder.ssoauth.service;

import com.cyancoder.sso.common.dto.*;
import com.cyancoder.ssoauth.client.CaptchaClient;
import com.cyancoder.ssoauth.client.FidoClient;
import com.cyancoder.ssoauth.client.OtpClient;
import com.cyancoder.ssoauth.client.SessionClient;
import com.cyancoder.ssoauth.client.UserClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final String LOGIN_PURPOSE = "LOGIN";

    private final UserClient userClient;
    private final CaptchaClient captchaClient;
    private final OtpClient otpClient;
    private final SessionClient sessionClient;
    private final FidoClient fidoClient;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserClient userClient,
            CaptchaClient captchaClient,
            OtpClient otpClient,
            SessionClient sessionClient,
            FidoClient fidoClient,
            JwtTokenService jwtTokenService,
            RefreshTokenService refreshTokenService
    ) {
        this.userClient = userClient;
        this.captchaClient = captchaClient;
        this.otpClient = otpClient;
        this.sessionClient = sessionClient;
        this.fidoClient = fidoClient;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenService = refreshTokenService;
    }

    public OtpSendResponse sendLoginOtp(OtpSendRequest request, String challengeId, String challengeAnswer) {
        CaptchaVerifyResponse captchaVerifyResponse = captchaClient.verify(
                new CaptchaVerifyRequest(challengeId, challengeAnswer, request.clientId())
        );
        if (!captchaVerifyResponse.success()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, captchaVerifyResponse.message());
        }

        UserSummary user = userClient.getUser(request.username());
        if (user == null || !user.active()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        if (!user.mfaEnabled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User does not require OTP login");
        }
        if (user.phoneNumber() == null || user.phoneNumber().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No phone number is registered for this account");
        }
        // Delivery target comes from the stored user record, never from the
        // request body, so a caller cannot redirect someone else's login code.
        return otpClient.send(new OtpSendRequest(
                request.username(), request.clientId(), LOGIN_PURPOSE, user.phoneNumber()
        ));
    }

    public TokenResponse login(LoginRequest request) {
        CaptchaVerifyResponse captchaVerifyResponse = captchaClient.verify(
                new CaptchaVerifyRequest(request.captchaChallengeId(), request.captchaAnswer(), request.clientId())
        );
        if (!captchaVerifyResponse.success()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, captchaVerifyResponse.message());
        }

        PasswordVerificationResponse passwordVerificationResponse = userClient.verifyPassword(
                new PasswordVerificationRequest(request.username(), request.password())
        );
        if (!passwordVerificationResponse.valid() || passwordVerificationResponse.user() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        UserSummary user = passwordVerificationResponse.user();
        if (user.mfaEnabled()) {
            if (request.otpCode() == null || request.otpCode().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP code is required");
            }
            OtpVerifyResponse otpVerifyResponse = otpClient.verify(
                    new OtpVerifyRequest(request.username(), request.clientId(), request.otpCode(), LOGIN_PURPOSE)
            );
            if (!otpVerifyResponse.success()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, otpVerifyResponse.message());
            }
        }

        SessionResponse sessionResponse = sessionClient.create(
                new SessionCreateRequest(user.username(), request.clientId(), request.deviceId())
        );
        IamUserAccessSummary access = userClient.resolveAccess(user.username(), request.clientId());
        return jwtTokenService.issue(request.clientId(), user, access, sessionResponse);
    }

    public SessionResponse logout(LogoutRequest request) {
        SessionResponse sessionResponse = sessionClient.revoke(request);
        if (sessionResponse != null) {
            refreshTokenService.revokeBySessionId(sessionResponse.sessionId());
        }
        return sessionResponse;
    }

    public TokenResponse refresh(RefreshTokenRequest request) {
        RefreshTokenService.RefreshTokenState state = refreshTokenService.consume(request.clientId(), request.refreshToken());
        if (state == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token invalid");
        }
        UserSummary user = userClient.getUser(state.subject());
        if (user == null || !user.active()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }
        SessionResponse sessionResponse = sessionClient.renew(state.sessionId(), new SessionRenewRequest(state.subject(), state.clientId()));
        if (sessionResponse == null || !sessionResponse.active()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session inactive");
        }
        IamUserAccessSummary access = userClient.resolveAccess(user.username(), request.clientId());
        return jwtTokenService.issue(request.clientId(), user, access, sessionResponse);
    }

    public TokenIntrospectionResponse introspect(TokenIntrospectionRequest request) {
        return refreshTokenService.introspect(request.token());
    }

    public FidoChallengeResponse startFido(FidoChallengeRequest request) {
        return fidoClient.challenge(request);
    }

    public FidoVerifyResponse verifyFido(FidoVerifyRequest request) {
        return fidoClient.verify(request);
    }
}
