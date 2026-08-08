package com.cyancoder.tenant.controller;

import com.cyancoder.platformopenapi.PlatformApiSecurity;
import com.cyancoder.platformopenapi.PlatformOpenApiAuth;
import com.cyancoder.tenant.api.TenantContracts.CreateTenantRequest;
import com.cyancoder.tenant.api.TenantContracts.EffectiveCapability;
import com.cyancoder.tenant.api.TenantContracts.TenantSummary;
import com.cyancoder.tenant.repository.TenantFeatureFlagRepository;
import com.cyancoder.tenant.service.TenantCapabilityService;
import com.cyancoder.tenant.service.TenantDirectoryService;
import com.cyancoder.tenant.service.TenantTeamService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    private final TenantTeamService team;

    public EndpointTenantController(TenantDirectoryService directory, TenantCapabilityService capabilities, TenantFeatureFlagRepository flags, TenantTeamService team) {
        this.directory = directory;
        this.capabilities = capabilities;
        this.flags = flags;
        this.team = team;
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

    @GetMapping("/{tenantKey}/permissions/catalog")
    public List<com.cyancoder.tenant.api.TenantContracts.PermissionDescriptor> permissions(@PathVariable String tenantKey) {
        return team.permissionCatalog(tenantKey);
    }

    @GetMapping("/{tenantKey}/roles")
    public List<com.cyancoder.tenant.api.TenantContracts.TenantRoleSummary> roles(@PathVariable String tenantKey) {
        return team.listRoles(tenantKey);
    }

    @PostMapping("/{tenantKey}/roles") @ResponseStatus(HttpStatus.CREATED)
    public com.cyancoder.tenant.api.TenantContracts.TenantRoleSummary createRole(@PathVariable String tenantKey,
            @Valid @RequestBody com.cyancoder.tenant.api.TenantContracts.SaveTenantRoleRequest request) {
        return team.saveRole(tenantKey, null, request);
    }

    @PutMapping("/{tenantKey}/roles/{roleKey}")
    public com.cyancoder.tenant.api.TenantContracts.TenantRoleSummary updateRole(@PathVariable String tenantKey, @PathVariable String roleKey,
            @Valid @RequestBody com.cyancoder.tenant.api.TenantContracts.SaveTenantRoleRequest request) {
        return team.saveRole(tenantKey, roleKey, request);
    }

    @DeleteMapping("/{tenantKey}/roles/{roleKey}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRole(@PathVariable String tenantKey, @PathVariable String roleKey) { team.deleteRole(tenantKey, roleKey); }

    @GetMapping("/{tenantKey}/users")
    public List<com.cyancoder.tenant.api.TenantContracts.TenantUserSummary> users(@PathVariable String tenantKey) { return team.listUsers(tenantKey); }

    @PostMapping("/{tenantKey}/users") @ResponseStatus(HttpStatus.CREATED)
    public com.cyancoder.tenant.api.TenantContracts.TenantUserSummary addUser(@PathVariable String tenantKey,
            @Valid @RequestBody com.cyancoder.tenant.api.TenantContracts.AddTenantUserRequest request) { return team.addUser(tenantKey, request); }

    @PutMapping("/{tenantKey}/users/{username}")
    public com.cyancoder.tenant.api.TenantContracts.TenantUserSummary updateUser(@PathVariable String tenantKey, @PathVariable String username,
            @Valid @RequestBody com.cyancoder.tenant.api.TenantContracts.UpdateTenantUserRequest request) { return team.updateUser(tenantKey, username, request); }

    @GetMapping("/{tenantKey}/users/{username}/effective-access")
    public com.cyancoder.tenant.api.TenantContracts.EffectiveAccess effectiveAccess(@PathVariable String tenantKey, @PathVariable String username) {
        directory.requireCurrentMembership(tenantKey); return team.effectiveAccess(tenantKey, username);
    }
}
