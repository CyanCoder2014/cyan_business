package com.cyancoder.searchindex.controller;

import com.cyancoder.dynamiccore.runtime.DynamicScopeResolver;
import com.cyancoder.searchindex.model.SearchQueryResponse;
import com.cyancoder.searchindex.model.SearchSuggestionResponse;
import com.cyancoder.searchindex.service.SearchQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/public/search-index")
public class PublicSearchIndexController {
    private final SearchQueryService searchQueryService;

    public PublicSearchIndexController(SearchQueryService searchQueryService) {
        this.searchQueryService = searchQueryService;
    }

    @GetMapping("/search")
    public SearchQueryResponse search(@RequestParam(required = false) String q,
                                      @RequestParam(required = false) String entityTypes,
                                      @RequestParam(required = false) String filterKey,
                                      @RequestParam(required = false) String filterValue,
                                      @RequestParam(defaultValue = "title_asc") String sort,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size,
                                      @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
                                      @RequestHeader(value = "X-Site-Key", required = false) String siteKey) {
        return searchQueryService.search(
                q,
                splitCsv(entityTypes),
                filterKey,
                filterValue,
                sort,
                page,
                size,
                DynamicScopeResolver.fromHeaders(tenantKey, siteKey)
        );
    }

    @GetMapping("/suggest")
    public SearchSuggestionResponse suggest(@RequestParam String q,
                                            @RequestParam(defaultValue = "8") int limit,
                                            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
                                            @RequestHeader(value = "X-Site-Key", required = false) String siteKey) {
        return searchQueryService.suggest(q, limit, DynamicScopeResolver.fromHeaders(tenantKey, siteKey));
    }

    private List<String> splitCsv(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }
}
