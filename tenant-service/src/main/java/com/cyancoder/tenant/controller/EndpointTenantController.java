package com.cyancoder.tenant.controller;

import com.cyancoder.platformopenapi.PlatformApiSecurity;
import com.cyancoder.platformopenapi.PlatformOpenApiAuth;
import com.cyancoder.tenant.api.TenantContracts.CreateTenantRequest;
import com.cyancoder.tenant.api.TenantContracts.EffectiveCapability;
import com.cyancoder.tenant.api.TenantContracts.TenantSummary;
import com.cyancoder.tenant.repository.TenantFeatureFlagRepository;
import com.cyancoder.tenant.service.TenantCapabilityService;
import com.cyancoder.tenant.service.TenantDirectoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/endpoint/tenants")
@PlatformOpenApiAuth(PlatformApiSecurity.BEARER)
public class EndpointTenantController {
    private final TenantDirectoryService directory;
    private final TenantCapabilityService capabilities;
    private final TenantFeatureFlagRepository flags;

    public EndpointTenantController(TenantDirectoryService directory, TenantCapabilityService capabilities, TenantFeatureFlagRepository flags) {
        this.directory = directory;
        this.capabilities = capabilities;
        this.flags = flags;
    }

    @GetMapping public List<TenantSummary> list() { return directory.listForCurrentUser(); }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public TenantSummary create(@Valid @RequestBody CreateTenantRequest request, @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return directory.create(request, idempotencyKey);
    }

    @GetMapping("/{tenantKey}")
    public TenantSummary get(@PathVariable String tenantKey) { return directory.getForCurrentUser(tenantKey); }

    @GetMapping("/{tenantKey}/capabilities")
    public List<EffectiveCapability> capabilities(@PathVariable String tenantKey, @RequestParam(required = false) String siteKey) {
        directory.requireCurrentMembership(tenantKey);
        return capabilities.resolve(tenantKey, siteKey);
    }

    @GetMapping("/{tenantKey}/feature-flags")
    public Map<String, String> featureFlags(@PathVariable String tenantKey) {
        directory.requireCurrentMembership(tenantKey);
        Map<String, String> result = new LinkedHashMap<>();
        flags.findByTenantKeyOrderByFlagKeyAsc(tenantKey).forEach(flag -> result.put(flag.getFlagKey(), flag.getFlagValue()));
        return result;
    }
}
