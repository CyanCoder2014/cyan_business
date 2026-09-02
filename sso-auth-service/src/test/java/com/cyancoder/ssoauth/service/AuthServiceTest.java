package com.cyancoder.ssoauth.service;

import com.cyancoder.sso.common.dto.*;
import com.cyancoder.ssoauth.client.CaptchaClient;
import com.cyancoder.ssoauth.client.FidoClient;
import com.cyancoder.ssoauth.client.OtpClient;
import com.cyancoder.ssoauth.client.SessionClient;
import com.cyancoder.ssoauth.client.UserClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Test
    void createsSessionForCanonicalUsernameWhenLoginUsesEmailAlias() {
        UserClient users = mock(UserClient.class);
        CaptchaClient captcha = mock(CaptchaClient.class);
        OtpClient otp = mock(OtpClient.class);
        SessionClient sessions = mock(SessionClient.class);
        FidoClient fido = mock(FidoClient.class);
        JwtTokenService tokens = mock(JwtTokenService.class);
        RefreshTokenService refreshTokens = mock(RefreshTokenService.class);
        AuthService service = new AuthService(users, captcha, otp, sessions, fido, tokens, refreshTokens);

        UserSummary user = new UserSummary("cyan-admin", "admin@cyan.local", null, false, List.of("ADMIN"), true);
        SessionResponse session = new SessionResponse("session-1", "cyan-admin", "panel-web", "browser", true, 1, 2);
        IamUserAccessSummary access = new IamUserAccessSummary("cyan-admin", "cyan", List.of("ADMIN"), List.of("*"), List.of());
        TokenResponse issued = new TokenResponse("access", "refresh", "Bearer", 900, "session-1");
        when(captcha.verify(any())).thenReturn(new CaptchaVerifyResponse(true, "ok"));
        when(users.verifyPassword(any())).thenReturn(new PasswordVerificationResponse(true, user));
        when(sessions.create(any())).thenReturn(session);
        when(users.resolveAccess("cyan-admin", "panel-web")).thenReturn(access);
        when(tokens.issue("panel-web", user, access, session)).thenReturn(issued);

        TokenResponse response = service.login(new LoginRequest(
                "panel-web", "admin@cyan.local", "admin123", "captcha", "answer", null, "browser"
        ));

        ArgumentCaptor<SessionCreateRequest> request = ArgumentCaptor.forClass(SessionCreateRequest.class);
        verify(sessions).create(request.capture());
        assertThat(request.getValue().username()).isEqualTo("cyan-admin");
        assertThat(response).isSameAs(issued);
    }

    @Test
    void refreshIsRejectedForASessionOlderThanTheLastPasswordChange() {
        UserClient users = mock(UserClient.class);
        SessionClient sessions = mock(SessionClient.class);
        RefreshTokenService refreshTokens = mock(RefreshTokenService.class);
        AuthService service = new AuthService(users, mock(CaptchaClient.class), mock(OtpClient.class),
                sessions, mock(FidoClient.class), mock(JwtTokenService.class), refreshTokens);

        Instant passwordChangedAt = Instant.parse("2026-03-01T12:00:00Z");
        UserSummary user = new UserSummary("cyan-admin", "admin@cyan.local", "0912", false,
                List.of("admin"), true, passwordChangedAt);
        // Session established an hour before the password was changed.
        SessionResponse session = new SessionResponse("session-1", "cyan-admin", "panel-web", "browser", true,
                passwordChangedAt.minusSeconds(3600).getEpochSecond(),
                passwordChangedAt.plusSeconds(86400).getEpochSecond());

        when(refreshTokens.consume("panel-web", "token")).thenReturn(
                new RefreshTokenService.RefreshTokenState("token", "cyan-admin", "panel-web", "session-1",
                        passwordChangedAt.plusSeconds(86400).getEpochSecond(), true));
        when(users.getUser("cyan-admin")).thenReturn(user);
        when(sessions.renew(eq("session-1"), any())).thenReturn(session);

        assertThatThrownBy(() -> service.refresh(new RefreshTokenRequest("panel-web", "token")))
                .hasMessageContaining("Credentials changed");
        // The token is burned so it cannot simply be retried.
        verify(refreshTokens).revokeBySessionId("session-1");
    }

    @Test
    void refreshStillWorksForASessionEstablishedAfterThePasswordChange() {
        UserClient users = mock(UserClient.class);
        SessionClient sessions = mock(SessionClient.class);
        RefreshTokenService refreshTokens = mock(RefreshTokenService.class);
        JwtTokenService tokens = mock(JwtTokenService.class);
        AuthService service = new AuthService(users, mock(CaptchaClient.class), mock(OtpClient.class),
                sessions, mock(FidoClient.class), tokens, refreshTokens);

        Instant passwordChangedAt = Instant.parse("2026-03-01T12:00:00Z");
        UserSummary user = new UserSummary("cyan-admin", "admin@cyan.local", "0912", false,
                List.of("admin"), true, passwordChangedAt);
        SessionResponse session = new SessionResponse("session-2", "cyan-admin", "panel-web", "browser", true,
                passwordChangedAt.plusSeconds(60).getEpochSecond(),
                passwordChangedAt.plusSeconds(86400).getEpochSecond());
        IamUserAccessSummary access = mock(IamUserAccessSummary.class);
        TokenResponse issued = mock(TokenResponse.class);

        when(refreshTokens.consume("panel-web", "token")).thenReturn(
                new RefreshTokenService.RefreshTokenState("token", "cyan-admin", "panel-web", "session-2",
                        passwordChangedAt.plusSeconds(86400).getEpochSecond(), true));
        when(users.getUser("cyan-admin")).thenReturn(user);
        when(sessions.renew(eq("session-2"), any())).thenReturn(session);
        when(users.resolveAccess("cyan-admin", "panel-web")).thenReturn(access);
        when(tokens.issue("panel-web", user, access, session)).thenReturn(issued);

        assertThat(service.refresh(new RefreshTokenRequest("panel-web", "token"))).isSameAs(issued);
        verify(refreshTokens, never()).revokeBySessionId(any());
    }
}
