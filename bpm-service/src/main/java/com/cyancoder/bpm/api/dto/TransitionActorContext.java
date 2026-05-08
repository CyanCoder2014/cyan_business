package com.cyancoder.bpm.api.dto;

import java.util.Set;

public record TransitionActorContext(String userId, Set<String> groups, Set<String> roles) {
    public Set<String> groupsOrEmpty() {
        return groups == null ? Set.of() : groups;
    }

    public Set<String> rolesOrEmpty() {
        return roles == null ? Set.of() : roles;
    }
}
