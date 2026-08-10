package com.cyancoder.tenant.service;

import com.cyancoder.tenant.api.TenantContracts.CreateTenantRequest;
import com.cyancoder.tenant.repository.*;
import com.cyancoder.tenant.security.TenantSecurity;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TenantDirectoryServiceTest {
    @Test void createsOwnerMembershipAndHonorsStableTenantKey() {
        TenantRepository tenants = mock(TenantRepository.class);
        TenantMembershipRepository memberships = mock(TenantMembershipRepository.class);
        IdempotencyRecordRepository idempotency = mock(IdempotencyRecordRepository.class);
        TenantSecurity security = mock(TenantSecurity.class);
        when(security.username()).thenReturn("owner@cyan.local");
        when(tenants.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        TenantDirectoryService service = new TenantDirectoryService(tenants, memberships, idempotency, security);

        var result = service.create(new CreateTenantRequest("north-star", "North Star"), "request-1");

        assertEquals("north-star", result.tenantKey());
        assertEquals("TENANT_OWNER", result.membershipRole());
        verify(memberships).save(argThat(value -> value.isActive() && value.getTenantKey().equals("north-star") && value.getUsername().equals("owner@cyan.local")));
        verify(idempotency).save(argThat(value -> value.getResourceKey().equals("north-star")));
    }
}
