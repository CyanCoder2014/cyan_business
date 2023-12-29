//package com.cyancoder.discoveryserver.config;
//
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cloud.netflix.eureka.http.EurekaClientHttpRequestFactorySupplier;
//import org.springframework.cloud.netflix.eureka.http.RestTemplateDiscoveryClientOptionalArgs;
//import org.springframework.cloud.netflix.eureka.http.RestTemplateTransportClientFactories;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.crypto.password.NoOpPasswordEncoder;
//import org.springframework.security.provisioning.InMemoryUserDetailsManager;
//import org.springframework.security.provisioning.JdbcUserDetailsManager;
//import org.springframework.security.provisioning.UserDetailsManager;
//import org.springframework.security.web.SecurityFilterChain;
//
//import javax.sql.DataSource;
//
//import java.util.function.Function;
//
//import static org.springframework.security.config.Customizer.withDefaults;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig     {
//
//
//    @Value("${eureka.username}")
//    private String username;
//    @Value("${eureka.password}")
//    private String password;
//
//
//    @Bean
//    public InMemoryUserDetailsManager userDetailsService() {
//        UserDetails user = User.withDefaultPasswordEncoder()
////                .passwordEncoder((Function<String, String>) NoOpPasswordEncoder.getInstance())
//                .username(username)
//                .password(password)
//                .roles("USER")
//                .build();
//        return new InMemoryUserDetailsManager(user);
//    }
//
////    @Bean
////    public UserDetailsManager users(DataSource dataSource) {
////        UserDetails user = User.withDefaultPasswordEncoder()
////                .username("admin")
////                .password("CyanAdmin")
////                .roles("USER")
////                .build();
////        JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);
////        users.createUser(user);
////        return users;
////    }
//
//
////    @Bean
////    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
////        http
////                .authorizeHttpRequests((authorize) -> authorize
////                        .anyRequest().authenticated()
////                )
////                .httpBasic(withDefaults());
////        return http.build();
////    }
//
//
//}
