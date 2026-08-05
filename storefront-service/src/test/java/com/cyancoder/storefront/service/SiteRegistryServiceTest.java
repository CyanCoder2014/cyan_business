package com.cyancoder.storefront.service;

import com.cyancoder.storefront.api.SiteContracts.CreateSiteRequest;
import com.cyancoder.storefront.repository.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SiteRegistryServiceTest {
    @Test void createsTenantScopedSiteWithIdempotencyRecord() {
        SiteRepository sites = mock(SiteRepository.class);
        SiteIdempotencyRepository idempotency = mock(SiteIdempotencyRepository.class);
        TenantMembershipClient memberships = mock(TenantMembershipClient.class);
        when(sites.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        SiteRegistryService service = new SiteRegistryService(sites, idempotency, memberships);

        var result = service.create("north-star", "owner@cyan.local", "request-1", new CreateSiteRequest("Main Store", null));

        assertEquals("main-store", result.siteKey());
        assertEquals("north-star", result.tenantKey());
        verify(memberships).requireMembership("north-star", "owner@cyan.local");
        verify(idempotency).save(any());
    }
}
