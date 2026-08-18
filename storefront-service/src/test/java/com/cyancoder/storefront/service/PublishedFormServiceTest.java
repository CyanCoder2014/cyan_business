package com.cyancoder.storefront.service;

import com.cyancoder.storefront.api.PublishedFormContracts.PublishFormRequest;
import com.cyancoder.storefront.model.PublishedFormEntity;
import com.cyancoder.storefront.repository.PublishedFormRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PublishedFormServiceTest {
    @Test
    void publishesOnlyAnActiveDefinitionInTheRequestedTenantScope() {
        PublishedFormRepository forms = mock(PublishedFormRepository.class);
        TenantMembershipClient memberships = mock(TenantMembershipClient.class);
        InternalServiceHttpSupport internal = mock(InternalServiceHttpSupport.class);
        when(internal.get(eq("crm-service"), eq("/internal/entities/definitions/contact-form"), eq("acme"), eq("main"), eq(Map.class)))
                .thenReturn(Map.of("active", true, "definition", Map.of("fields", Map.of("email", Map.of("type", "string")))));
        when(forms.findBySlug("contact-us")).thenReturn(Optional.empty());
        when(forms.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        PublishedFormService service = new PublishedFormService(forms, memberships, internal);

        var result = service.publish("acme", "main", "owner", new PublishFormRequest(
                "contact-us", "crm-service", "contact-form", "Contact us", "Send a request", "PUBLIC"));

        assertEquals("contact-us", result.slug());
        assertEquals("PUBLIC", result.visibility());
        verify(memberships).requireMembership("acme", "owner");
        verify(memberships).requirePermission("acme", "owner", "definition.manage");
    }

    @Test
    void privateFormIsNeverResolvedByTheAnonymousEndpoint() {
        PublishedFormRepository forms = mock(PublishedFormRepository.class);
        PublishedFormEntity privateForm = new PublishedFormEntity("staff-request", "acme", "", "crm-service", "request", "Staff request", null, "AUTHENTICATED", "owner");
        when(forms.findBySlug("staff-request")).thenReturn(Optional.of(privateForm));
        PublishedFormService service = new PublishedFormService(forms, mock(TenantMembershipClient.class), mock(InternalServiceHttpSupport.class));

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.getPublic("staff-request"));
        assertEquals(404, error.getStatusCode().value());
    }

    @Test
    void repeatedSubmissionKeyTargetsTheSameDynamicRecord() {
        PublishedFormRepository forms = mock(PublishedFormRepository.class);
        InternalServiceHttpSupport internal = mock(InternalServiceHttpSupport.class);
        PublishedFormEntity form = new PublishedFormEntity("lead", "acme", "main", "crm-service", "lead-form", "Lead", null, "PUBLIC", "owner");
        setId(form, 41L);
        when(forms.findBySlug("lead")).thenReturn(Optional.of(form));
        PublishedFormService service = new PublishedFormService(forms, mock(TenantMembershipClient.class), internal);

        var first = service.submitPublic("lead", "request-7", Map.of("email", "person@example.test"));
        var second = service.submitPublic("lead", "request-7", Map.of("email", "person@example.test"));

        assertEquals(first.submissionKey(), second.submissionKey());
        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(internal, times(2)).post(eq("crm-service"), path.capture(), eq("acme"), eq("main"), any(), eq(Map.class));
        assertEquals(path.getAllValues().get(0), path.getAllValues().get(1));
    }

    private static void setId(PublishedFormEntity entity, Long id) {
        try {
            var field = PublishedFormEntity.class.getDeclaredField("id"); field.setAccessible(true); field.set(entity, id);
        } catch (ReflectiveOperationException exception) { throw new AssertionError(exception); }
    }
}
