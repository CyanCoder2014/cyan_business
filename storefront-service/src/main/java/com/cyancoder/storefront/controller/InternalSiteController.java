package com.cyancoder.storefront.controller;

import com.cyancoder.storefront.api.SiteContracts.SiteMembership;
import com.cyancoder.storefront.service.SiteRegistryService;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/internal/sites")
public class InternalSiteController {
    private final SiteRegistryService service;
    public InternalSiteController(SiteRegistryService service) { this.service = service; }
    @GetMapping("/{siteKey}") public SiteMembership membership(@PathVariable String siteKey, @RequestParam String tenantKey) { return service.internalMembership(tenantKey, siteKey); }
}
