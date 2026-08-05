package com.cyancoder.tenant.controller;

import com.cyancoder.platformopenapi.PlatformApiSecurity;
import com.cyancoder.platformopenapi.PlatformOpenApiAuth;
import com.cyancoder.tenant.api.TenantContracts.MembershipAccess;
import com.cyancoder.tenant.service.TenantDirectoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/tenants")
@PlatformOpenApiAuth(PlatformApiSecurity.BASIC)
public class InternalTenantController {
    private final TenantDirectoryService directory;

    public InternalTenantController(TenantDirectoryService directory) { this.directory = directory; }

    @GetMapping("/{tenantKey}/members/{username}/access")
    public MembershipAccess membership(@PathVariable String tenantKey, @PathVariable String username) {
        return directory.internalMembership(tenantKey, username);
    }
}
