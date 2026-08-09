package com.cyancoder.tenant.service;

import com.cyancoder.tenant.api.TenantContracts.*;
import com.cyancoder.tenant.model.TenantMembershipEntity;
import com.cyancoder.tenant.model.TenantRoleEntity;
import com.cyancoder.tenant.repository.TenantMembershipRepository;
import com.cyancoder.tenant.repository.TenantRoleRepository;
import com.cyancoder.tenant.security.TenantSecurity;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class TenantTeamService {
    private static final List<PermissionDescriptor> CATALOG = List.of(
            permission("team.read", "Team", "View team", "View tenant users and roles"),
            permission("team.manage", "Team", "Manage team", "Add, suspend, and assign tenant users"),
            permission("roles.manage", "Team", "Manage roles", "Create roles and grant bounded permissions"),
            permission("settings.read", "Workspace", "View settings", "View tenant configuration"),
            permission("settings.manage", "Workspace", "Manage settings", "Change tenant configuration"),
            permission("billing.read", "Billing", "View billing", "View plan, usage, and invoices"),
            permission("billing.manage", "Billing", "Manage billing", "Change the tenant subscription"),
            permission("project.read", "Build", "View projects", "View provisioned projects"),
            permission("project.create", "Build", "Create projects", "Provision platform projects"),
            permission("definition.read", "Build", "View definitions", "View dynamic definitions"),
            permission("definition.manage", "Build", "Manage definitions", "Create and publish definitions"),
            permission("record.read", "Operate", "View records", "View tenant business records"),
            permission("record.manage", "Operate", "Manage records", "Create and update tenant records"),
            permission("bpm.read", "Operate", "View BPM", "View workflows and work queues"),
            permission("bpm.manage", "Operate", "Manage BPM", "Create and publish workflows"),
            permission("automation.read", "Operate", "View automation", "View automation definitions and runs"),
            permission("automation.manage", "Operate", "Manage automation", "Create and publish automations"),
            permission("report.read", "Operate", "View reports", "View report definitions and runs"),
            permission("report.manage", "Operate", "Manage reports", "Create definitions and execute reports"),
            permission("media.read", "Operate", "View media", "View tenant media assets"),
            permission("media.manage", "Operate", "Manage media", "Upload and update tenant media"),
            permission("search.read", "Operate", "View search", "View indexes and test search"),
            permission("search.manage", "Operate", "Manage search", "Create indexes and run synchronization"),
            permission("site.read", "Experience", "View sites", "View tenant sites"),
            permission("site.manage", "Experience", "Manage sites", "Create and publish tenant sites")
    );
    private static final Set<String> KEYS = CATALOG.stream().map(PermissionDescriptor::key).collect(java.util.stream.Collectors.toUnmodifiableSet());

    private final TenantMembershipRepository memberships;
    private final TenantRoleRepository roles;
    private final TenantDirectoryService tenants;
    private final TenantSecurity security;
    private final IdentityDirectoryClient identities;

    public TenantTeamService(TenantMembershipRepository memberships, TenantRoleRepository roles,
                             TenantDirectoryService tenants, TenantSecurity security, IdentityDirectoryClient identities) {
        this.memberships = memberships; this.roles = roles; this.tenants = tenants; this.security = security; this.identities = identities;
    }

    public List<PermissionDescriptor> permissionCatalog(String tenantKey) { requireRead(tenantKey); return CATALOG; }

    @Transactional
    public List<TenantRoleSummary> listRoles(String tenantKey) {
        requireRead(tenantKey); ensureSystemRoles(tenantKey);
        return roles.findByTenantKeyOrderBySystemRoleDescDisplayNameAsc(tenantKey).stream().map(this::summary).toList();
    }

    @Transactional
    public TenantRoleSummary saveRole(String tenantKey, String pathRoleKey, SaveTenantRoleRequest request) {
        requireManageRoles(tenantKey); ensureSystemRoles(tenantKey);
        String roleKey = request.roleKey().trim().toUpperCase(Locale.ROOT);
        if (pathRoleKey != null && !pathRoleKey.equals(roleKey)) throw new IllegalArgumentException("Role key cannot be changed");
        validatePermissions(tenantKey, request.permissions());
        TenantRoleEntity role = roles.findByTenantKeyAndRoleKey(tenantKey, roleKey).orElse(null);
        Instant now = Instant.now();
        if (role == null) {
            role = new TenantRoleEntity(); role.setRoleId(tenantKey + "|" + roleKey); role.setTenantKey(tenantKey);
            role.setRoleKey(roleKey); role.setSystemRole(false); role.setCreatedAt(now);
        } else {
            if (role.isSystemRole()) throw new IllegalArgumentException("System roles cannot be edited");
            if (request.expectedRevision() == null || request.expectedRevision() != role.getRevision()) throw new OptimisticLockingFailureException("Role was changed by another user");
        }
        role.setDisplayName(request.displayName().trim()); role.setDescription(trim(request.description()));
        role.setPermissions(new LinkedHashSet<>(request.permissions())); role.setUpdatedAt(now);
        return summary(roles.save(role));
    }

    @Transactional
    public void deleteRole(String tenantKey, String roleKey) {
        requireManageRoles(tenantKey); ensureSystemRoles(tenantKey);
        TenantRoleEntity role = requireRole(tenantKey, roleKey);
        if (role.isSystemRole()) throw new IllegalArgumentException("System roles cannot be deleted");
        if (memberships.countByTenantKeyAndRoleKeyAndActiveTrue(tenantKey, roleKey) > 0) throw new IllegalArgumentException("Remove active members before deleting this role");
        roles.delete(role);
    }

    @Transactional
    public List<TenantUserSummary> listUsers(String tenantKey) {
        requireRead(tenantKey); ensureSystemRoles(tenantKey);
        return memberships.findByTenantKeyOrderByUsernameAsc(tenantKey).stream().map(this::userSummary).toList();
    }

    @Transactional
    public TenantUserSummary addUser(String tenantKey, AddTenantUserRequest request) {
        requireManageTeam(tenantKey); ensureSystemRoles(tenantKey); requireAssignableRole(tenantKey, request.roleKey());
        TenantMembershipEntity existing = memberships.findByTenantKeyAndUsername(tenantKey, request.username()).orElse(null);
        if (existing != null && existing.isActive()) throw new IllegalArgumentException("User is already an active tenant member");
        IdentityDirectoryClient.IdentityUser identity;
        if (request.initialPassword() != null && !request.initialPassword().isBlank()) {
            identity = identities.provision(request.username(), request.initialPassword(), request.email(), request.phoneNumber(), request.mfaRequired());
        } else {
            identity = identities.get(request.username());
            if (identity == null) throw new IllegalArgumentException("An initial password is required for a new identity");
        }
        Instant now = Instant.now();
        TenantMembershipEntity membership = existing == null ? new TenantMembershipEntity() : existing;
        membership.setMembershipId(tenantKey + "|" + identity.username()); membership.setTenantKey(tenantKey);
        membership.setUsername(identity.username()); membership.setRoleKey(request.roleKey()); membership.setActive(true);
        if (existing == null) membership.setCreatedAt(now); membership.setUpdatedAt(now);
        return userSummary(memberships.save(membership), identity);
    }

    @Transactional
    public TenantUserSummary updateUser(String tenantKey, String username, UpdateTenantUserRequest request) {
        requireManageTeam(tenantKey); ensureSystemRoles(tenantKey); requireAssignableRole(tenantKey, request.roleKey());
        TenantMembershipEntity membership = memberships.findByTenantKeyAndUsername(tenantKey, username).orElseThrow(NoSuchElementException::new);
        if ((!request.active() || !"TENANT_OWNER".equals(request.roleKey())) && "TENANT_OWNER".equals(membership.getRoleKey())
                && memberships.countByTenantKeyAndRoleKeyAndActiveTrue(tenantKey, "TENANT_OWNER") <= 1) {
            throw new IllegalArgumentException("The last tenant owner cannot be suspended or demoted");
        }
        membership.setRoleKey(request.roleKey()); membership.setActive(request.active()); membership.setUpdatedAt(Instant.now());
        return userSummary(memberships.save(membership));
    }

    @Transactional
    public EffectiveAccess effectiveAccess(String tenantKey, String username) {
        ensureSystemRoles(tenantKey);
        TenantMembershipEntity membership = memberships.findByTenantKeyAndUsernameAndActiveTrue(tenantKey, username)
                .orElseThrow(() -> new AccessDeniedException("Tenant membership is required"));
        TenantRoleEntity role = requireRole(tenantKey, membership.getRoleKey());
        return new EffectiveAccess(tenantKey, username, role.getRoleKey(), List.copyOf(role.getPermissions()), true);
    }

    private void requireRead(String tenantKey) { tenants.requireCurrentMembership(tenantKey); }
    private void requireManageTeam(String tenantKey) { requirePermission(tenantKey, "team.manage"); }
    private void requireManageRoles(String tenantKey) { requirePermission(tenantKey, "roles.manage"); }
    private void requirePermission(String tenantKey, String permission) {
        if (security.isPlatformAdmin()) return;
        EffectiveAccess access = effectiveAccess(tenantKey, security.username());
        if (!access.permissions().contains("*") && !access.permissions().contains(permission)) throw new AccessDeniedException("Permission is required: " + permission);
    }
    private void validatePermissions(String tenantKey, List<String> requested) {
        if (requested.stream().anyMatch(key -> !KEYS.contains(key))) throw new IllegalArgumentException("Unknown permission key");
        if (security.isPlatformAdmin()) return;
        Set<String> caller = Set.copyOf(effectiveAccess(tenantKey, security.username()).permissions());
        if (!caller.contains("*") && !caller.containsAll(requested)) throw new AccessDeniedException("Cannot grant permissions you do not hold");
    }
    private void requireAssignableRole(String tenantKey, String roleKey) {
        TenantRoleEntity target = requireRole(tenantKey, roleKey);
        if (security.isPlatformAdmin()) return;
        Set<String> caller = Set.copyOf(effectiveAccess(tenantKey, security.username()).permissions());
        if (!caller.contains("*") && !caller.containsAll(target.getPermissions())) throw new AccessDeniedException("Cannot assign a role with broader access");
    }
    private TenantRoleEntity requireRole(String tenantKey, String roleKey) { return roles.findByTenantKeyAndRoleKey(tenantKey, roleKey).orElseThrow(NoSuchElementException::new); }

    private void ensureSystemRoles(String tenantKey) {
        Instant now = Instant.now();
        upsertSystemRole(tenantKey, "TENANT_OWNER", "Owner", Set.of("*"), now);
        upsertSystemRole(tenantKey, "TENANT_ADMIN", "Administrator", KEYS, now);
        upsertSystemRole(tenantKey, "TENANT_MEMBER", "Member", Set.of("project.read", "definition.read", "record.read", "bpm.read", "automation.read", "report.read", "media.read", "search.read", "site.read", "settings.read"), now);
    }
    private void upsertSystemRole(String tenantKey,String key,String name,Set<String> permissions,Instant now){TenantRoleEntity role=roles.findByTenantKeyAndRoleKey(tenantKey,key).orElseGet(()->systemRole(tenantKey,key,name,permissions,now));if(!role.getPermissions().equals(permissions)){role.setPermissions(new LinkedHashSet<>(permissions));role.setUpdatedAt(now);roles.save(role);}else if(role.getRoleId()!=null&&!roles.existsById(role.getRoleId()))roles.save(role);}
    private TenantRoleEntity systemRole(String tenantKey, String key, String name, Set<String> permissions, Instant now) {
        TenantRoleEntity role = new TenantRoleEntity(); role.setRoleId(tenantKey + "|" + key); role.setTenantKey(tenantKey);
        role.setRoleKey(key); role.setDisplayName(name); role.setDescription("Platform-managed tenant role"); role.setSystemRole(true);
        role.setPermissions(new LinkedHashSet<>(permissions)); role.setCreatedAt(now); role.setUpdatedAt(now); return role;
    }
    private TenantRoleSummary summary(TenantRoleEntity role) { return new TenantRoleSummary(role.getRoleKey(), role.getDisplayName(), role.getDescription(), role.isSystemRole(), role.getRevision(), List.copyOf(role.getPermissions()), memberships.countByTenantKeyAndRoleKeyAndActiveTrue(role.getTenantKey(), role.getRoleKey())); }
    private TenantUserSummary userSummary(TenantMembershipEntity membership) {
        IdentityDirectoryClient.IdentityUser identity = identities.get(membership.getUsername()); return userSummary(membership, identity);
    }
    private TenantUserSummary userSummary(TenantMembershipEntity membership, IdentityDirectoryClient.IdentityUser identity) { return new TenantUserSummary(membership.getUsername(), identity == null ? null : identity.email(), identity == null ? null : identity.phoneNumber(), membership.getRoleKey(), membership.isActive(), membership.getCreatedAt(), membership.getUpdatedAt()); }
    private static PermissionDescriptor permission(String key, String group, String name, String description) { return new PermissionDescriptor(key, group, name, description); }
    private static String trim(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
