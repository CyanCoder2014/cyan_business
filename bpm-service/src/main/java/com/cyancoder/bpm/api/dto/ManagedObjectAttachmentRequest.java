package com.cyancoder.bpm.api.dto;

import java.util.Map;
import java.util.Set;

public record ManagedObjectAttachmentRequest(
        String stateId,
        String assetKey,
        String fileName,
        String downloadUrl,
        String contentType,
        Long sizeBytes,
        Set<String> visibleToUserIds,
        Set<String> visibleToRoles,
        Set<String> visibleToGroups,
        String visibleUntilState,
        Map<String, Object> metadata
) {
}
