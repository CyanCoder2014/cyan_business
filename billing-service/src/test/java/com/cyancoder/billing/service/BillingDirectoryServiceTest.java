package com.cyancoder.billing.service;

import com.cyancoder.billing.repository.*;
import com.cyancoder.billing.security.BillingSecurity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BillingDirectoryServiceTest {
    @Test void reportsTruthfulNotConfiguredStateWhenNoSubscriptionExists() {
        PlanRepository plans = mock(PlanRepository.class);
        TenantSubscriptionRepository subscriptions = mock(TenantSubscriptionRepository.class);
        when(subscriptions.findById("north-star")).thenReturn(Optional.empty());
        TenantMembershipClient memberships = mock(TenantMembershipClient.class);
        BillingSecurity security = mock(BillingSecurity.class);
        when(security.username()).thenReturn("owner@cyan.local");
        BillingDirectoryService service = new BillingDirectoryService(plans, subscriptions, mock(BillingIdempotencyRepository.class), mock(TenantUsageCounterRepository.class), memberships, security, new ObjectMapper());

        var result = service.subscriptionForCurrentUser("north-star");

        assertEquals("NONE", result.status());
        assertEquals("NOT_CONFIGURED", result.providerState());
        assertTrue(result.features().isEmpty());
        verify(memberships).require("north-star", "owner@cyan.local");
    }
}
