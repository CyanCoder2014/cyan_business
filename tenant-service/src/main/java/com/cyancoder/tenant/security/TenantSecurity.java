package com.cyancoder.tenant.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class TenantSecurity {
    public String username() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AccessDeniedException("Authenticated JWT is required");
        }
        return jwt.getSubject();
    }

    public boolean isPlatformAdmin() {
        Set<String> permissions = permissions();
        return permissions.contains("*") || permissions.contains("realm:*") || permissions.contains("realm:manage");
    }

    @SuppressWarnings("unchecked")
    private Set<String> permissions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) return Set.of();
        LinkedHashSet<String> result = new LinkedHashSet<>();
        Map<String, Object> realm = jwt.getClaimAsMap("realm_access");
        if (realm != null) result.addAll(strings(realm.get("permissions")));
        Map<String, Object> resources = jwt.getClaimAsMap("resource_access");
        if (resources != null) resources.values().forEach(value -> {
            if (value instanceof Map<?, ?> map) result.addAll(strings(map.get("permissions")));
        });
        return result;
    }

    private List<String> strings(Object value) {
        return value instanceof List<?> list ? list.stream().map(String::valueOf).toList() : List.of();
    }
}
