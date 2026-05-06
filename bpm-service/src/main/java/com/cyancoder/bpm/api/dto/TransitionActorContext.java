package com.cyancoder.bpm.api.dto;

import java.util.Set;

public record TransitionActorContext(String userId, Set<String> groups, Set<String> roles) {
}

