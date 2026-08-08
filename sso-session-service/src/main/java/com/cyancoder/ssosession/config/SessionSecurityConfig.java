package com.cyancoder.ssosession.config;

import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class SessionSecurityConfig {
    @Bean @Order(0)
    SecurityFilterChain internalSecurity(HttpSecurity http) throws Exception {
        http.securityMatcher("/internal/**").csrf(c -> c.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a.anyRequest().hasRole("INTERNAL"))
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
    @Bean @Order(1)
    SecurityFilterChain scopeSecurity(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/sso/sessions/*/scope").csrf(c -> c.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a.anyRequest().authenticated())
                .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()));
        return http.build();
    }
    @Bean @Order(2)
    SecurityFilterChain legacySecurity(HttpSecurity http) throws Exception {
        http.csrf(c -> c.disable()).authorizeHttpRequests(a -> a.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    UserDetailsService internalUsers(
            @Value("${sso-session.internal.username:sso_session_internal}") String username,
            @Value("${sso-session.internal.password:sso_session_secret}") String password) {
        return new InMemoryUserDetailsManager(User.withUsername(username).password("{noop}" + password).roles("INTERNAL").build());
    }
}
