package com.cyancoder.billing.controller;

import com.cyancoder.billing.api.BillingContracts.*;
import com.cyancoder.billing.service.BillingDirectoryService;
import com.cyancoder.platformopenapi.PlatformApiSecurity;
import com.cyancoder.platformopenapi.PlatformOpenApiAuth;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/endpoint/billing")
@PlatformOpenApiAuth(PlatformApiSecurity.BEARER)
public class EndpointBillingController {
    private final BillingDirectoryService service;
    public EndpointBillingController(BillingDirectoryService service) { this.service = service; }
    @GetMapping("/plans") public List<PlanSummary> plans() { return service.listPlans(); }
    @PostMapping("/plans") public PlanSummary upsert(@Valid @RequestBody PlanUpsertRequest request) { return service.upsertPlan(request); }
    @GetMapping("/tenants/{tenantKey}/subscription") public SubscriptionSummary subscription(@PathVariable String tenantKey) { return service.subscriptionForCurrentUser(tenantKey); }
    @PostMapping("/tenants/{tenantKey}/subscription/change") public SubscriptionSummary change(@PathVariable String tenantKey, @Valid @RequestBody ChangeSubscriptionRequest request, @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return service.change(tenantKey, request, idempotencyKey);
    }
}
