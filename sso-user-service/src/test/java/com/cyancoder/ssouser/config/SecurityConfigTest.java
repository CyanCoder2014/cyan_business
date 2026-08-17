package com.cyancoder.ssouser.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {
    @Test
    void internalPasswordIsStoredAsBcryptAndMatchesTheConfiguredSecret() {
        SecurityConfig config = new SecurityConfig();
        PasswordEncoder encoder = config.passwordEncoder();
        UserDetails user = config.internalUser("sso_user_internal", "shared-secret", encoder)
                .loadUserByUsername("sso_user_internal");

        assertThat(user.getPassword()).startsWith("$2");
        assertThat(encoder.matches("shared-secret", user.getPassword())).isTrue();
        assertThat(encoder.matches("wrong-secret", user.getPassword())).isFalse();
    }
}
