package com.cyancoder.tenant.service;

import com.cyancoder.tenant.api.TenantContracts.UpdateTenantUserRequest;
import com.cyancoder.tenant.model.TenantMembershipEntity;
import com.cyancoder.tenant.model.TenantRoleEntity;
import com.cyancoder.tenant.repository.TenantMembershipRepository;
import com.cyancoder.tenant.repository.TenantRoleRepository;
import com.cyancoder.tenant.security.TenantSecurity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TenantTeamServiceTest {
    @Test void publishesCanonicalAiPermissions() {
        TenantSecurity security=mock(TenantSecurity.class);when(security.isPlatformAdmin()).thenReturn(true);
        TenantTeamService service=new TenantTeamService(mock(TenantMembershipRepository.class),mock(TenantRoleRepository.class),mock(TenantDirectoryService.class),security,mock(IdentityDirectoryClient.class));
        var keys=service.permissionCatalog("acme").stream().map(com.cyancoder.tenant.api.TenantContracts.PermissionDescriptor::key).collect(java.util.stream.Collectors.toSet());
        org.junit.jupiter.api.Assertions.assertTrue(keys.containsAll(Set.of("ai.read","ai.execute","automation.execute")));
    }

    @Test void protectsLastTenantOwnerFromDemotion() {
        TenantMembershipRepository memberships=mock(TenantMembershipRepository.class);
        TenantRoleRepository roles=mock(TenantRoleRepository.class);
        TenantSecurity security=mock(TenantSecurity.class);
        when(security.isPlatformAdmin()).thenReturn(true);
        TenantMembershipEntity owner=membership("acme","head","TENANT_OWNER");
        when(memberships.findByTenantKeyAndUsername("acme","head")).thenReturn(Optional.of(owner));
        when(memberships.countByTenantKeyAndRoleKeyAndActiveTrue("acme","TENANT_OWNER")).thenReturn(1L);
        when(roles.findByTenantKeyOrderBySystemRoleDescDisplayNameAsc("acme")).thenReturn(List.of(role("acme","TENANT_OWNER",Set.of("*")),role("acme","TENANT_MEMBER",Set.of("record.read"))));
        when(roles.findByTenantKeyAndRoleKey("acme","TENANT_MEMBER")).thenReturn(Optional.of(role("acme","TENANT_MEMBER",Set.of("record.read"))));
        TenantTeamService service=new TenantTeamService(memberships,roles,mock(TenantDirectoryService.class),security,mock(IdentityDirectoryClient.class));

        assertThrows(IllegalArgumentException.class,()->service.updateUser("acme","head",new UpdateTenantUserRequest("TENANT_MEMBER",true)));
        verify(memberships,never()).save(any());
    }

    @Test void rejectsRoleThatGrantsPermissionCallerDoesNotHold() {
        TenantMembershipRepository memberships=mock(TenantMembershipRepository.class);
        TenantRoleRepository roles=mock(TenantRoleRepository.class);
        TenantSecurity security=mock(TenantSecurity.class);when(security.username()).thenReturn("manager");
        TenantMembershipEntity manager=membership("acme","manager","LIMITED_ADMIN");
        TenantRoleEntity limited=role("acme","LIMITED_ADMIN",Set.of("roles.manage","record.read"));
        when(memberships.findByTenantKeyAndUsernameAndActiveTrue("acme","manager")).thenReturn(Optional.of(manager));
        when(roles.findByTenantKeyOrderBySystemRoleDescDisplayNameAsc("acme")).thenReturn(List.of(limited));
        when(roles.findByTenantKeyAndRoleKey("acme","LIMITED_ADMIN")).thenReturn(Optional.of(limited));
        TenantTeamService service=new TenantTeamService(memberships,roles,mock(TenantDirectoryService.class),security,mock(IdentityDirectoryClient.class));

        var request=new com.cyancoder.tenant.api.TenantContracts.SaveTenantRoleRequest("POWER_USER","Power user",null,List.of("billing.manage"),null);
        assertThrows(org.springframework.security.access.AccessDeniedException.class,()->service.saveRole("acme",null,request));
    }

    private static TenantMembershipEntity membership(String tenant,String username,String role){TenantMembershipEntity value=new TenantMembershipEntity();value.setMembershipId(tenant+"|"+username);value.setTenantKey(tenant);value.setUsername(username);value.setRoleKey(role);value.setActive(true);value.setCreatedAt(Instant.now());value.setUpdatedAt(Instant.now());return value;}
    private static TenantRoleEntity role(String tenant,String key,Set<String> permissions){TenantRoleEntity value=new TenantRoleEntity();value.setRoleId(tenant+"|"+key);value.setTenantKey(tenant);value.setRoleKey(key);value.setDisplayName(key);value.setPermissions(permissions);value.setCreatedAt(Instant.now());value.setUpdatedAt(Instant.now());return value;}
}
