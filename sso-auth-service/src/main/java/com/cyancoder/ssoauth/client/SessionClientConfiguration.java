package com.cyancoder.ssoauth.client;

import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class SessionClientConfiguration {
    @Bean
    BasicAuthRequestInterceptor sessionBasicAuth(
            @Value("${sso-session.internal.username:sso_session_internal}") String username,
            @Value("${sso-session.internal.password:sso_session_secret}") String password) {
        return new BasicAuthRequestInterceptor(username, password);
    }
}
