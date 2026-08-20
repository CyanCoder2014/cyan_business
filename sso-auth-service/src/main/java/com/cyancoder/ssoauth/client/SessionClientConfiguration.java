package com.cyancoder.ssoauth.client;

import com.cyancoder.platform.internalhttp.InternalServiceCredentials;
import com.cyancoder.platform.internalhttp.InternalServiceCredentialsResolver;
import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.context.annotation.Bean;

public class SessionClientConfiguration {
    @Bean
    BasicAuthRequestInterceptor sessionBasicAuth(InternalServiceCredentialsResolver credentialsResolver) {
        InternalServiceCredentials credentials = credentialsResolver.resolve("sso-session-service");
        return new BasicAuthRequestInterceptor(credentials.username(), credentials.password());
    }
}
