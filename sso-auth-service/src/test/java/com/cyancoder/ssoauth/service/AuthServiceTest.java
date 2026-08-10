package com.cyancoder.ssoauth.service;

import com.cyancoder.sso.common.dto.*;
import com.cyancoder.ssoauth.client.CaptchaClient;
import com.cyancoder.ssoauth.client.FidoClient;
import com.cyancoder.ssoauth.client.OtpClient;
import com.cyancoder.ssoauth.client.SessionClient;
import com.cyancoder.ssoauth.client.UserClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
}
