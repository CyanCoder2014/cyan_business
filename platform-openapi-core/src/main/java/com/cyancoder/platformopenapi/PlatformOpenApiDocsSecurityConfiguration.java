package com.cyancoder.platformopenapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(SecurityFilterChain.class)
public class PlatformOpenApiDocsSecurityConfiguration {

    @Bean
    @Order(0)
    SecurityFilterChain platformOpenApiSecurityFilterChain(
            HttpSecurity http,
            PlatformOpenApiProperties properties,
            @Value("${service.internal.username:docs_internal}") String internalUsername,
            @Value("${service.internal.password:docs_secret}") String internalPassword
    ) throws Exception {
        http.securityMatcher(
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml",
                        "/swagger-ui.html",
                        "/swagger-ui/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        if (properties.getDocsAccess() == PlatformOpenApiProperties.DocsAccess.PUBLIC) {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        } else if (properties.getDocsAccess() == PlatformOpenApiProperties.DocsAccess.DISABLED) {
            http.authorizeHttpRequests(auth -> auth.anyRequest().denyAll());
        } else {
            UserDetailsService docsUsers = docsUsers(
                    properties, internalUsername, internalPassword);
            DaoAuthenticationProvider provider = new DaoAuthenticationProvider(docsUsers);
            provider.setPasswordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder());
            http.authorizeHttpRequests(auth -> auth.anyRequest().hasRole("API_DOCS"))
                    .authenticationProvider(provider)
                    .httpBasic(Customizer.withDefaults());
        }
        return http.build();
    }

    private UserDetailsService docsUsers(
            PlatformOpenApiProperties properties,
            String internalUsername,
            String internalPassword
    ) {
        String username = properties.getDocsUsername() == null || properties.getDocsUsername().isBlank()
                ? internalUsername
                : properties.getDocsUsername();
        String password = properties.getDocsPassword() == null || properties.getDocsPassword().isBlank()
                ? internalPassword
                : properties.getDocsPassword();
        return new InMemoryUserDetailsManager(User.withUsername(username)
                .password("{noop}" + password)
                .roles("API_DOCS")
                .build());
    }
}
