package com.cyancoder.searchindex.model;

import java.util.List;

public record SearchSuggestionResponse(
        String query,
        List<String> suggestions
) {
}
