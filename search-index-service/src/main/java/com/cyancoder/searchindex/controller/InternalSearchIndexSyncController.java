package com.cyancoder.searchindex.controller;

import com.cyancoder.searchindex.model.SearchIndexSyncResponse;
import com.cyancoder.searchindex.service.SearchIndexSyncService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/search-index")
public class InternalSearchIndexSyncController {
    private final SearchIndexSyncService searchIndexSyncService;

    public InternalSearchIndexSyncController(SearchIndexSyncService searchIndexSyncService) {
        this.searchIndexSyncService = searchIndexSyncService;
    }

    @PostMapping("/sync/{sourceServiceKey}/{sourceEntityKey}")
    public SearchIndexSyncResponse sync(@PathVariable String sourceServiceKey, @PathVariable String sourceEntityKey) {
        return searchIndexSyncService.sync(sourceServiceKey, sourceEntityKey);
    }
}
