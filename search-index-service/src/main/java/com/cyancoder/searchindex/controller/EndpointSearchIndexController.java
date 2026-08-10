package com.cyancoder.searchindex.controller;

import com.cyancoder.dynamiccore.runtime.DynamicScopeResolver;
import com.cyancoder.searchindex.model.SearchIndexSyncResponse;
import com.cyancoder.searchindex.model.SearchQueryResponse;
import com.cyancoder.searchindex.model.SearchSuggestionResponse;
import com.cyancoder.searchindex.service.SearchIndexSyncService;
import com.cyancoder.searchindex.service.SearchAdminService;
import com.cyancoder.searchindex.service.TenantMembershipClient;
import com.cyancoder.searchindex.model.SearchAdminContracts.*;
import org.springframework.security.core.Authentication;
import com.cyancoder.searchindex.service.SearchQueryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/endpoint/search-index")
public class EndpointSearchIndexController {
    private final SearchQueryService searchQueryService;
    private final SearchIndexSyncService searchIndexSyncService;
    private final SearchAdminService adminService;
    private final TenantMembershipClient memberships;

    public EndpointSearchIndexController(SearchQueryService searchQueryService, SearchIndexSyncService searchIndexSyncService, SearchAdminService adminService,TenantMembershipClient memberships) {
        this.searchQueryService = searchQueryService;
        this.searchIndexSyncService = searchIndexSyncService;
        this.adminService = adminService;
        this.memberships = memberships;
    }

    @GetMapping("/search")
    @PreAuthorize("@platformAuthorizationService.canReadService('search-index-service')")
    public SearchQueryResponse search(@RequestParam(required = false) String q,
                                      @RequestParam(required = false) String entityTypes,
                                      @RequestParam(required = false) String filterKey,
                                      @RequestParam(required = false) String filterValue,
                                      @RequestParam(defaultValue = "title_asc") String sort,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size,
                                      @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
                                      @RequestHeader(value = "X-Site-Key", required = false) String siteKey) {
        memberships.requireMembership(tenantKey,org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName());
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
    @PreAuthorize("@platformAuthorizationService.canReadService('search-index-service')")
    public SearchSuggestionResponse suggest(@RequestParam String q,
                                            @RequestParam(defaultValue = "8") int limit,
                                            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
                                            @RequestHeader(value = "X-Site-Key", required = false) String siteKey) {
        memberships.requireMembership(tenantKey,org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName());
        return searchQueryService.suggest(q, limit, DynamicScopeResolver.fromHeaders(tenantKey, siteKey));
    }

    @PostMapping("/sync/{sourceServiceKey}/{sourceEntityKey}")
    @PreAuthorize("@platformAuthorizationService.canManageService('search-index-service')")
    public SearchSyncRunView sync(@PathVariable String sourceServiceKey, @PathVariable String sourceEntityKey,
            @RequestHeader("X-Tenant-Key") String tenantKey,@RequestHeader(value="X-Site-Key",required=false)String siteKey,
            @RequestHeader("Idempotency-Key")String idempotencyKey, Authentication authentication) {
        return adminService.run(tenantKey,siteKey,authentication.getName(),sourceServiceKey,sourceEntityKey,idempotencyKey);
    }

    @GetMapping("/definitions") @PreAuthorize("@platformAuthorizationService.canReadService('search-index-service')") public List<IndexView> definitions(@RequestHeader("X-Tenant-Key")String t,@RequestHeader(value="X-Site-Key",required=false)String s,Authentication a){return adminService.list(t,s,a.getName());}
    @GetMapping("/definitions/{key}") @PreAuthorize("@platformAuthorizationService.canReadService('search-index-service')") public IndexView definition(@PathVariable String key,@RequestHeader("X-Tenant-Key")String t,@RequestHeader(value="X-Site-Key",required=false)String s,Authentication a){return adminService.get(t,s,a.getName(),key);}
    @PostMapping("/definitions") @PreAuthorize("@platformAuthorizationService.canManageService('search-index-service')") public IndexView create(@RequestHeader("X-Tenant-Key")String t,@RequestHeader(value="X-Site-Key",required=false)String s,Authentication a,@RequestBody SaveIndexRequest r){return adminService.save(t,s,a.getName(),null,r);}
    @org.springframework.web.bind.annotation.PutMapping("/definitions/{key}") @PreAuthorize("@platformAuthorizationService.canManageService('search-index-service')") public IndexView update(@PathVariable String key,@RequestHeader("X-Tenant-Key")String t,@RequestHeader(value="X-Site-Key",required=false)String s,Authentication a,@RequestBody SaveIndexRequest r){return adminService.save(t,s,a.getName(),key,r);}
    @GetMapping("/sync-runs") @PreAuthorize("@platformAuthorizationService.canReadService('search-index-service')") public SearchSyncRunPage runs(@RequestHeader("X-Tenant-Key")String t,@RequestHeader(value="X-Site-Key",required=false)String s,Authentication a,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size){return adminService.history(t,s,a.getName(),page,size);}
    @GetMapping("/stats") @PreAuthorize("@platformAuthorizationService.canReadService('search-index-service')") public SearchIndexStats stats(@RequestHeader("X-Tenant-Key")String t,@RequestHeader(value="X-Site-Key",required=false)String s,Authentication a,@RequestParam String indexKey){return adminService.stats(t,s,a.getName(),indexKey);}

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
