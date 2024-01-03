package com.cyancoder.taxpaysys.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.DelegatingJwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class DirectlyConfiguredJwkSetUri {



    @Bean
    SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        DelegatingJwtGrantedAuthoritiesConverter authoritiesConverter =
                // Using the delegating converter multiple converters can be combined
                new DelegatingJwtGrantedAuthoritiesConverter(
                        // First add the default converter
                        new JwtGrantedAuthoritiesConverter(),
                        // Second add our custom Keycloak specific converter
                        new KeycloakJwtRolesConverter());

        // Set up http security to use the JWT converter defined above
        httpSecurity.oauth2ResourceServer().jwt().jwtAuthenticationConverter(
                jwt -> new JwtAuthenticationToken(jwt, authoritiesConverter.convert(jwt)));

        httpSecurity.authorizeHttpRequests(authorize -> authorize
                // Only users with the role "user" can access the endpoint /user.
//                .requestMatchers("/**").hasAuthority(KeycloakJwtRolesConverter.PREFIX_RESOURCE_ROLE + "cyan-business_user")
                .requestMatchers("/**").permitAll()
        );

        return httpSecurity.build();
    }
}
