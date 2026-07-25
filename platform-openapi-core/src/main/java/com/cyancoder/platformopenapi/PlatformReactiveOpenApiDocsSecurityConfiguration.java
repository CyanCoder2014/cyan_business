package com.cyancoder.platformopenapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(SecurityWebFilterChain.class)
public class PlatformReactiveOpenApiDocsSecurityConfiguration {

    @Bean
    @Order(0)
    SecurityWebFilterChain platformReactiveOpenApiSecurityFilterChain(
            ServerHttpSecurity http,
            PlatformOpenApiProperties properties,
            @Value("${service.internal.username:docs_internal}") String internalUsername,
            @Value("${service.internal.password:docs_secret}") String internalPassword
    ) {
        http.securityMatcher(ServerWebExchangeMatchers.pathMatchers(
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml",
                        "/swagger-ui.html",
                        "/swagger-ui/**"))
                .csrf(ServerHttpSecurity.CsrfSpec::disable);
        if (properties.getDocsAccess() == PlatformOpenApiProperties.DocsAccess.PUBLIC) {
            http.authorizeExchange(auth -> auth.anyExchange().permitAll());
        } else if (properties.getDocsAccess() == PlatformOpenApiProperties.DocsAccess.DISABLED) {
            http.authorizeExchange(auth -> auth.anyExchange().denyAll());
        } else {
            String username = properties.getDocsUsername() == null
                    || properties.getDocsUsername().isBlank()
                    ? internalUsername
                    : properties.getDocsUsername();
            String password = properties.getDocsPassword() == null
                    || properties.getDocsPassword().isBlank()
                    ? internalPassword
                    : properties.getDocsPassword();
            UserDetails docsUser = User.withUsername(username)
                    .password("{noop}" + password)
                    .roles("API_DOCS")
                    .build();
            UserDetailsRepositoryReactiveAuthenticationManager manager =
                    new UserDetailsRepositoryReactiveAuthenticationManager(
                            new MapReactiveUserDetailsService(docsUser));
            manager.setPasswordEncoder(
                    PasswordEncoderFactories.createDelegatingPasswordEncoder());
            http.authorizeExchange(auth -> auth.anyExchange().hasRole("API_DOCS"))
                    .authenticationManager(manager)
                    .httpBasic(Customizer.withDefaults());
        }
        return http.build();
    }
}
