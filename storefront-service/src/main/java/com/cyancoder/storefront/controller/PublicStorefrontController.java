package com.cyancoder.storefront.controller;

import com.cyancoder.dynamiccore.runtime.DynamicScopeResolver;
import com.cyancoder.storefront.model.ResolvedRouteResponse;
import com.cyancoder.storefront.service.StorefrontRouteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/public/storefront")
public class PublicStorefrontController {
    private final StorefrontRouteService storefrontRouteService;

    public PublicStorefrontController(StorefrontRouteService storefrontRouteService) {
        this.storefrontRouteService = storefrontRouteService;
    }

    @GetMapping("/resolve")
    public ResolvedRouteResponse resolve(
            @RequestParam String path,
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey
    ) {
        return storefrontRouteService.resolve(path, DynamicScopeResolver.fromHeaders(tenantKey, siteKey));
    }

    @GetMapping("/render")
    public Map<String, Object> render(
            @RequestParam String path,
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey
    ) {
        return storefrontRouteService.render(path, DynamicScopeResolver.fromHeaders(tenantKey, siteKey));
    }

    @GetMapping(value = "/page", produces = "text/html;charset=UTF-8")
    public String page(
            @RequestParam String path,
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey
    ) {
        return storefrontRouteService.renderHtml(path, DynamicScopeResolver.fromHeaders(tenantKey, siteKey));
    }

    @GetMapping("/sitemap")
    public List<Map<String, Object>> sitemap(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey
    ) {
        return storefrontRouteService.sitemap(DynamicScopeResolver.fromHeaders(tenantKey, siteKey));
    }
}
