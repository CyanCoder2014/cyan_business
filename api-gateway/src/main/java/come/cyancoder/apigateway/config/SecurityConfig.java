package come.cyancoder.apigateway.config;


import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {


    @Bean
    public SecurityWebFilterChain springSecurityWebFilterChain(ServerHttpSecurity serverHttpSecurity, GlobalCorsProperties corsProperties){
        serverHttpSecurity		.cors(cors -> cors.disable())
                .csrf()
                .disable()
                .cors(corsSpec -> corsSpec.configurationSource(unused -> corsProperties.getCorsConfigurations().get("/**")))
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/eureka/**")
                        .permitAll()
                        .pathMatchers("/realms/cyan/protocol/openid-connect/**")
                        .permitAll()
                        .anyExchange()
                        .authenticated()
                )
                .oauth2ResourceServer(ServerHttpSecurity.OAuth2ResourceServerSpec::jwt);
        return serverHttpSecurity.build();

    }


    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
