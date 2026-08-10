package com.cyancoder.storefront.controller;

import com.cyancoder.storefront.api.SiteContracts.*;
import com.cyancoder.storefront.service.SiteRegistryService;
import com.cyancoder.storefront.service.InternalServiceHttpSupport;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/endpoint/sites")
public class EndpointSiteController {
    private final SiteRegistryService service;
    private final InternalServiceHttpSupport http;
    public EndpointSiteController(SiteRegistryService service, InternalServiceHttpSupport http) { this.service = service; this.http = http; }
    @GetMapping public List<SiteSummary> list(@RequestHeader("X-Tenant-Key") String tenantKey, Authentication auth) { return service.list(tenantKey, auth.getName()); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public SiteSummary create(@RequestHeader("X-Tenant-Key") String tenantKey, @RequestHeader("Idempotency-Key") String key, @Valid @RequestBody CreateSiteRequest request, Authentication auth) { return service.create(tenantKey, auth.getName(), key, request); }
    @GetMapping("/{siteKey}/portal/work")
    public Object portalWork(@PathVariable String siteKey,
                             @RequestHeader("X-Tenant-Key") String tenantKey,
                             @RequestParam(defaultValue="ASSIGNED") String view,
                             @RequestParam(required=false) String state,
                             @RequestParam(required=false) String priority,
                             @RequestParam(required=false) Boolean overdue,
                             @RequestParam(required=false) String query,
                             @RequestParam(defaultValue="0") int page,
                             @RequestParam(defaultValue="20") int size,
                             Authentication auth) {
        service.requireSiteMembership(tenantKey, siteKey, auth.getName());
        String roles = auth.getAuthorities().stream().map(org.springframework.security.core.GrantedAuthority::getAuthority).collect(java.util.stream.Collectors.joining(","));
        String groups = auth.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt jwt && jwt.getClaimAsString("groups") != null ? jwt.getClaimAsString("groups") : "";
        String path = "/internal/bpm/managed-objects/cartable?view=" + encode(view) + "&page=" + Math.max(0,page) + "&size=" + Math.max(1,Math.min(size,100))
                + optional("state",state) + optional("priority",priority) + optional("overdue",overdue==null?null:overdue.toString()) + optional("query",query);
        return http.getAsActor("bpm-service", path, tenantKey, siteKey, auth.getName(), roles, groups, Object.class);
    }
    private String optional(String key,String value){return value==null||value.isBlank()?"":"&"+key+"="+encode(value);}
    private String encode(String value){return java.net.URLEncoder.encode(value,java.nio.charset.StandardCharsets.UTF_8);}
}
