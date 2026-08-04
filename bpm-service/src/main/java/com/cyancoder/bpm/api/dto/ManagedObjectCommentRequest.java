package com.cyancoder.bpm.api.dto;

import java.util.Map;
import java.util.Set;

public record ManagedObjectCommentRequest(
        String stateId,
        String body,
        Set<String> visibleToUserIds,
        Set<String> visibleToRoles,
        Set<String> visibleToGroups,
        String visibleUntilState,
        Map<String, Object> metadata
) {
}
