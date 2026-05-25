package com.cyancoder.searchindex.model;

import java.util.List;
import java.util.Map;

public record SearchQueryResponse(
        String query,
        int page,
        int size,
        long total,
        List<Map<String, Object>> items,
        Map<String, Long> facetCounts
) {
}
