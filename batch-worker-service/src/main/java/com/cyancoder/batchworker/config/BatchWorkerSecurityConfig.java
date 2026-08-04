package com.cyancoder.batchworker.config;

import com.cyancoder.dynamiccore.security.InternalSecurityProperties;
import com.cyancoder.dynamiccore.security.PlatformAuthorizationService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@Import(PlatformAuthorizationService.class)
@EnableConfigurationProperties({InternalSecurityProperties.class, BatchWorkerProperties.class})
public class BatchWorkerSecurityConfig {
    @Bean
    @Order(1)
    SecurityFilterChain internal(HttpSecurity http) throws Exception {
        http.securityMatcher("/internal/**").csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain endpoint(HttpSecurity http) throws Exception {
        http.securityMatcher("/endpoint/**").csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    @Order(3)
    SecurityFilterChain fallback(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/error").permitAll().anyRequest().denyAll());
        return http.build();
    }

    @Bean
    UserDetailsService users(InternalSecurityProperties properties) {
        return new InMemoryUserDetailsManager(User.withUsername(properties.getUsername())
                .password("{noop}" + properties.getPassword()).roles("INTERNAL").build());
    }
}
