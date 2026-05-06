package com.cyancoder.bpm.service;

import com.cyancoder.bpm.api.dto.TransitionActorContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ActorContextResolver {

    public TransitionActorContext fromAuthentication(Authentication authentication) {
        if (authentication == null) {
            return new TransitionActorContext("system", Set.of(), Set.of());
        }
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> groups = new LinkedHashSet<>();
        String userId = authentication.getName();
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            userId = firstNonBlank(jwt.getClaimAsString("sub"), jwt.getClaimAsString("preferred_username"), authentication.getName());
            groups.addAll(csvClaim(jwt.getClaimAsString("groups")));
        }
        return new TransitionActorContext(userId == null || userId.isBlank() ? "system" : userId, groups, roles);
    }

    public TransitionActorContext fromInternalHeaders(String userId, String roles, String groups) {
        return new TransitionActorContext(
                userId == null || userId.isBlank() ? "system" : userId,
                csvClaim(groups),
                csvClaim(roles)
        );
    }

    private Set<String> csvClaim(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}

