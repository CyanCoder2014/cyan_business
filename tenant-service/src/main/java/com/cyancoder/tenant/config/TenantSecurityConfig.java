package com.cyancoder.tenant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class TenantSecurityConfig {
    @Bean @Order(1)
    SecurityFilterChain internal(HttpSecurity http) throws Exception {
        http.securityMatcher("/internal/**").csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated()).httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean @Order(2)
    SecurityFilterChain endpoint(HttpSecurity http) throws Exception {
        http.securityMatcher("/endpoint/**").csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean @Order(3)
    SecurityFilterChain fallback(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/error").permitAll().anyRequest().denyAll());
        return http.build();
    }

    @Bean
    UserDetailsService internalUser(
            @Value("${service.internal.username:tenant_internal}") String username,
            @Value("${service.internal.password:tenant_secret}") String password
    ) {
        return new InMemoryUserDetailsManager(User.withUsername(username).password("{noop}" + password).roles("INTERNAL").build());
    }
}
