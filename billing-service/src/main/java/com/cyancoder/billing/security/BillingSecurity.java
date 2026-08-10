package com.cyancoder.billing.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class BillingSecurity {
    public String username() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) throw new AccessDeniedException("Authenticated JWT is required");
        return jwt.getSubject();
    }
    public void requirePlatformAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) throw new AccessDeniedException("Authenticated JWT is required");
        Map<String, Object> realm = jwt.getClaimAsMap("realm_access");
        List<String> roles = realm != null && realm.get("roles") instanceof List<?> list ? list.stream().map(String::valueOf).toList() : List.of();
        List<String> permissions = realm != null && realm.get("permissions") instanceof List<?> list ? list.stream().map(String::valueOf).toList() : List.of();
        if (!roles.contains("super-admin") && !permissions.contains("*")) throw new AccessDeniedException("Platform admin permission required");
    }
}
