package com.cyancoder.storefront.controller;

import com.cyancoder.storefront.api.SiteContracts.*;
import com.cyancoder.storefront.service.SiteRegistryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/endpoint/sites")
public class EndpointSiteController {
    private final SiteRegistryService service;
    public EndpointSiteController(SiteRegistryService service) { this.service = service; }
    @GetMapping public List<SiteSummary> list(@RequestHeader("X-Tenant-Key") String tenantKey, Authentication auth) { return service.list(tenantKey, auth.getName()); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public SiteSummary create(@RequestHeader("X-Tenant-Key") String tenantKey, @RequestHeader("Idempotency-Key") String key, @Valid @RequestBody CreateSiteRequest request, Authentication auth) { return service.create(tenantKey, auth.getName(), key, request); }
}
