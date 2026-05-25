package com.cyancoder.searchindex.model;

public record SearchIndexSyncResponse(
        String sourceServiceKey,
        String sourceEntityKey,
        int syncedCount
) {
}
