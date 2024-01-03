package com.cyancoder.client.config;


import org.springframework.stereotype.Component;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;


@Component
public class OAuthToken {

    JwtAuthenticationToken oauthToken;

    OAuthToken(){

    }

    @Bean
    @Scope("request")
    public String getToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        oauthToken = (JwtAuthenticationToken) authentication;
        return oauthToken.getToken().getTokenValue();
    }

    @Bean
    @Scope("request")
    public String getAttribute(String name) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        oauthToken = (JwtAuthenticationToken) authentication;
        return oauthToken.getTokenAttributes().get(name).toString();
    }

}
