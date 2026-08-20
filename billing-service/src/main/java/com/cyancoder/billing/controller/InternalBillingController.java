package com.cyancoder.billing.controller;

import com.cyancoder.billing.api.BillingContracts.BillingEntitlements;
import com.cyancoder.billing.service.BillingDirectoryService;
import com.cyancoder.platformopenapi.PlatformApiSecurity;
import com.cyancoder.platformopenapi.PlatformOpenApiAuth;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/billing")
@PlatformOpenApiAuth(PlatformApiSecurity.BASIC)
public class InternalBillingController {
    private final BillingDirectoryService service;
    public InternalBillingController(BillingDirectoryService service) { this.service = service; }
    @GetMapping("/tenants/{tenantKey}/entitlements") public BillingEntitlements entitlements(@PathVariable String tenantKey) { return service.internalEntitlements(tenantKey); }
    @PostMapping("/tenants/{tenantKey}/usage/increment")
    public void incrementUsage(@PathVariable String tenantKey, @org.springframework.web.bind.annotation.RequestBody com.cyancoder.billing.api.BillingContracts.UsageIncrementRequest request) {
        service.incrementUsage(tenantKey, request);
    }
    @PostMapping("/tenants/{tenantKey}/subscription/change")
    public com.cyancoder.billing.api.BillingContracts.SubscriptionSummary change(@PathVariable String tenantKey,
            @RequestBody com.cyancoder.billing.api.BillingContracts.ChangeSubscriptionRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return service.internalChange(tenantKey, request, idempotencyKey);
    }
}
