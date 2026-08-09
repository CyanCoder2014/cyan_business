package com.cyancoder.tenant.controller;

import com.cyancoder.platformopenapi.PlatformApiSecurity;
import com.cyancoder.platformopenapi.PlatformOpenApiAuth;
import com.cyancoder.tenant.api.TenantContracts.MembershipAccess;
import com.cyancoder.tenant.service.TenantDirectoryService;
import com.cyancoder.tenant.service.TenantTeamService;
import com.cyancoder.tenant.service.TenantCapabilityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/tenants")
@PlatformOpenApiAuth(PlatformApiSecurity.BASIC)
public class InternalTenantController {
    private final TenantDirectoryService directory;
    private final TenantTeamService team;
    private final TenantCapabilityService capabilities;

    public InternalTenantController(TenantDirectoryService directory, TenantTeamService team, TenantCapabilityService capabilities) { this.directory = directory; this.team = team; this.capabilities = capabilities; }

    @GetMapping("/{tenantKey}/members/{username}/access")
    public MembershipAccess membership(@PathVariable String tenantKey, @PathVariable String username) {
        return directory.internalMembership(tenantKey, username);
    }

    @GetMapping("/{tenantKey}/members/{username}/effective-access")
    public com.cyancoder.tenant.api.TenantContracts.EffectiveAccess effectiveAccess(@PathVariable String tenantKey, @PathVariable String username) {
        return team.effectiveAccess(tenantKey, username);
    }

    @GetMapping("/{tenantKey}/capabilities")
    public java.util.List<com.cyancoder.tenant.api.TenantContracts.EffectiveCapability> capabilities(
            @PathVariable String tenantKey,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String siteKey) {
        return capabilities.resolve(tenantKey, siteKey);
    }
}
