package com.cyancoder.dynamiccore.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component("platformAuthorizationService")
public class PlatformAuthorizationService {

    public boolean hasAnyPermission(String... permissions) {
        Set<String> granted = grantedPermissions();
        for (String permission : permissions) {
            if (permission == null || permission.isBlank()) {
                continue;
            }
            if (matches(granted, permission)) {
                return true;
            }
        }
        return false;
    }

    public boolean canReadService(String serviceKey) {
        return hasAnyPermission("*", "panel:*", "builder:*", "service:*", "service:" + serviceKey + ":read", "service:" + serviceKey + ":write", "service:" + serviceKey + ":manage");
    }

    public boolean canWriteService(String serviceKey) {
        return hasAnyPermission("*", "panel:*", "builder:*", "service:*", "service:" + serviceKey + ":write", "service:" + serviceKey + ":manage");
    }

    public boolean canManageService(String serviceKey) {
        return hasAnyPermission("*", "panel:*", "builder:*", "iam:*", "service:*", "service:" + serviceKey + ":manage");
    }

    public boolean canUseCapability(String capabilityKey) {
        return hasAnyPermission("*", "panel:*", capabilityKey, capabilityPrefix(capabilityKey));
    }

    @SuppressWarnings("unchecked")
    private Set<String> grantedPermissions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return Set.of();
        }
        LinkedHashSet<String> granted = new LinkedHashSet<>();
        granted.addAll(stringList(jwt.getClaimAsMap("realm_access") == null ? null : jwt.getClaimAsMap("realm_access").get("permissions")));
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess != null) {
            resourceAccess.values().forEach(value -> {
                if (value instanceof Map<?, ?> map) {
                    granted.addAll(stringList(map.get("permissions")));
                }
            });
        }
        return granted;
    }

    private boolean matches(Set<String> granted, String required) {
        if (granted.contains(required) || granted.contains("*")) {
            return true;
        }
        String prefix = capabilityPrefix(required);
        return !prefix.equals(required) && granted.contains(prefix);
    }

    private String capabilityPrefix(String permission) {
        int index = permission.indexOf(':');
        return index > 0 ? permission.substring(0, index) + ":*" : permission;
    }

    private List<String> stringList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }
}
