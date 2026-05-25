package com.cyancoder.ssouser.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class IamSecurityService {
    public String currentUsername() {
        return jwt().getSubject();
    }

    public String currentClientId() {
        return jwt().getClaimAsString("client_id");
    }

    public String currentRealmKey() {
        return jwt().getClaimAsString("realm");
    }

    public boolean hasRealmRole(String roleKey) {
        return realmRoles().contains(roleKey);
    }

    public boolean hasRealmPermission(String permission) {
        return realmPermissions().contains("*") || realmPermissions().contains(permission) || realmPermissions().contains(permissionPrefix(permission));
    }

    public boolean isSuperAdmin() {
        return hasRealmRole("super-admin") || hasRealmPermission("*");
    }

    public boolean hasClientPermission(String clientId, String permission) {
        return clientPermissions(clientId).contains("*")
                || clientPermissions(clientId).contains(permission)
                || clientPermissions(clientId).contains(permissionPrefix(permission));
    }

    public void requirePlatformAdmin() {
        if (!hasRealmRole("super-admin") && !hasRealmPermission("realm:create")) {
            deny("Platform admin permission required");
        }
    }

    public void requireRealmManager(String realmKey) {
        if (!Objects.equals(currentRealmKey(), realmKey)) {
            if (hasRealmRole("super-admin")) {
                return;
            }
            deny("Cross-realm management is not allowed");
        }
        if (!(hasRealmRole("super-admin")
                || hasRealmPermission("realm:manage")
                || hasRealmPermission("client:create")
                || hasRealmPermission("user:manage"))) {
            deny("Realm management permission required");
        }
    }

    public void requireClientManager(String realmKey, String clientId) {
        if (hasRealmRole("super-admin")) {
            return;
        }
        if (!Objects.equals(currentRealmKey(), realmKey)) {
            deny("Cross-realm client management is not allowed");
        }
        if (hasRealmPermission("client:manage") || hasRealmPermission("user:manage") || hasRealmPermission("client-role:assign")) {
            return;
        }
        if (!Objects.equals(currentClientId(), clientId)) {
            deny("Only the owning client admin may manage this client");
        }
        if (!(hasClientPermission(clientId, "client:manage")
                || hasClientPermission(clientId, "user:manage")
                || hasClientPermission(clientId, "client-role:assign"))) {
            deny("Client management permission required");
        }
    }

    public void requireClientScopedUserProvision(String realmKey, String clientId) {
        if (hasRealmRole("super-admin")) {
            return;
        }
        if (clientId == null || clientId.isBlank()) {
            requireRealmManager(realmKey);
            return;
        }
        requireClientManager(realmKey, clientId);
    }

    private List<String> realmRoles() {
        Map<String, Object> realmAccess = jwt().getClaimAsMap("realm_access");
        Object raw = realmAccess == null ? null : realmAccess.get("roles");
        return stringList(raw);
    }

    private Set<String> realmPermissions() {
        Map<String, Object> realmAccess = jwt().getClaimAsMap("realm_access");
        Object raw = realmAccess == null ? null : realmAccess.get("permissions");
        return new LinkedHashSet<>(stringList(raw));
    }

    private Set<String> clientPermissions(String clientId) {
        Map<String, Object> resourceAccess = jwt().getClaimAsMap("resource_access");
        if (resourceAccess == null) {
            return Set.of();
        }
        Object client = resourceAccess.get(clientId);
        if (!(client instanceof Map<?, ?> map)) {
            return Set.of();
        }
        return new LinkedHashSet<>(stringList(map.get("permissions")));
    }

    @SuppressWarnings("unchecked")
    private List<String> stringList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    private Jwt jwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            deny("Authenticated JWT is required");
        }
        return (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private String permissionPrefix(String permission) {
        int index = permission.indexOf(':');
        return index > 0 ? permission.substring(0, index) + ":*" : permission;
    }

    private void deny(String message) {
        throw new org.springframework.security.access.AccessDeniedException(message);
    }
}
