package com.cyancoder.aiorchestrator.service.impl;

import com.cyancoder.aiorchestrator.api.dto.UpdateDraftRequest;
import com.cyancoder.aiorchestrator.domain.AppBlueprint;
import com.cyancoder.aiorchestrator.domain.ClientAppDraft;
import com.cyancoder.aiorchestrator.domain.DraftStatus;
import com.cyancoder.aiorchestrator.domain.EntityBlueprint;
import com.cyancoder.aiorchestrator.repo.ClientAppDraftRepository;
import com.cyancoder.aiorchestrator.service.BlueprintCatalogService;
import com.cyancoder.aiorchestrator.service.ServiceAvailabilityResolver;
import com.cyancoder.aiorchestrator.service.ServiceAvailabilitySnapshot;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MongoAppDraftServiceEcommerceScenarioTest {

    private ClientAppDraftRepository repository;
    private BlueprintCatalogService blueprintCatalogService;
    private MongoAppDraftService service;

    @BeforeEach
    void setUp() {
        repository = mock(ClientAppDraftRepository.class);
        blueprintCatalogService = mock(BlueprintCatalogService.class);

        AppBlueprint ecommerceBlueprint = new SeedBlueprintFactory().createDefaults().stream()
                .filter(item -> "ecommerce-crm-zarinpal-v1".equals(item.getBlueprintKey()))
                .findFirst()
                .orElseThrow();

        when(blueprintCatalogService.resolveActiveByType("e-commerce")).thenReturn(ecommerceBlueprint);
        when(blueprintCatalogService.getActiveByBlueprintKey("ecommerce-crm-zarinpal-v1")).thenReturn(ecommerceBlueprint);
        when(repository.save(any(ClientAppDraft.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.findFirstByTenantKeyAndSiteKeyAndClientKeyAndAppTypeOrderByUpdatedAtDesc(
                "tenant-spiffy", "site-spiffy", "client-spiffy", "e-commerce"
        )).thenReturn(Optional.empty());

        DefaultFollowUpQuestionService followUpQuestionService = new DefaultFollowUpQuestionService(blueprintCatalogService);
        ServiceAvailabilityResolver availabilityResolver = mock(ServiceAvailabilityResolver.class);
        when(availabilityResolver.resolve(any())).thenReturn(new ServiceAvailabilitySnapshot(
                List.of("content-service", "catalog-service", "storefront-service", "crm-service",
                        "commerce-service", "checkout-service", "notification-service",
                        "payment-orchestrator-service"),
                "TEST"));
        service = new MongoAppDraftService(repository, blueprintCatalogService, followUpQuestionService,
                new ObjectMapper(), availabilityResolver);
    }

    @Test
    void resolvesBlueprintDraftForSpiffyLikeCommercePromptAndAsksRequiredQuestions() {
        ClientAppDraft draft = service.resolveKnownAppDraft(
                null,
                "tenant-spiffy",
                "site-spiffy",
                "client-spiffy",
                "want a e-commerce site like the one have before with spiffy, with CRM, notify user and connect to zarinpal for payment then user orders"
        ).orElseThrow();

        assertEquals("ecommerce-crm-zarinpal-v1", draft.getBlueprintKey());
        assertEquals("e-commerce", draft.getAppType());
        assertEquals(DraftStatus.WAITING_FOR_ANSWERS, draft.getStatus());
        assertEquals("zarinpal-default", String.valueOf(draft.getAnswers().get("paymentProvider")));
        assertTrue(draft.getPendingQuestionKeys().containsAll(List.of(
                "brandName",
                "homePageTitle",
                "starterProductName",
                "subdomainPrefix",
                "pageContentSummary"
        )));
    }

    @Test
    void draftBecomesReadyAndEnrichesEntitiesAfterRequiredAnswersAreProvided() {
        ClientAppDraft draft = service.resolveKnownAppDraft(
                null,
                "tenant-spiffy",
                "site-spiffy",
                "client-spiffy",
                "want a e-commerce site like the one have before with spiffy, with CRM, notify user and connect to zarinpal for payment then user orders"
        ).orElseThrow();

        when(repository.findByDraftId(draft.getDraftId())).thenReturn(Optional.of(draft));

        ClientAppDraft updated = service.updateDraft(
                draft.getDraftId(),
                new UpdateDraftRequest(
                        null,
                        null,
                        Map.of(
                                "brandName", "Spiffy",
                                "homePageTitle", "Spiffy Storefront",
                                "starterProductName", "Spiffy Starter Box",
                                "pageContentSummary", "Launch a polished ecommerce experience with CRM, notifications, and Zarinpal payments.",
                                "subdomainPrefix", "spiffy"
                        )
                ),
                "test-user"
        );

        assertEquals(DraftStatus.READY, updated.getStatus());
        assertTrue(updated.getPendingQuestionKeys().isEmpty());
        assertEquals("spiffy.cyan.local", updated.getResolvedDsl().getApp().getDesiredDomain());

        EntityBlueprint product = updated.getResolvedDsl().getEntities().stream()
                .filter(entity -> "catalog-service".equals(entity.getServiceKey()))
                .findFirst()
                .orElseThrow();
        assertEquals("Spiffy Starter Box", product.getRecordData().get("name"));
        assertEquals("STARTER-001", product.getRecordData().get("sku"));
        assertEquals("spiffy-starter-box", product.getRecordData().get("slug"));

        EntityBlueprint landingPage = updated.getResolvedDsl().getEntities().stream()
                .filter(entity -> "content-service".equals(entity.getServiceKey()) && "landing-home".equals(entity.getRecordKey()))
                .findFirst()
                .orElseThrow();
        assertEquals("Spiffy Storefront", landingPage.getRecordData().get("title"));
        assertEquals(
                "Launch a polished ecommerce experience with CRM, notifications, and Zarinpal payments.",
                landingPage.getRecordData().get("heroSubtitle")
        );

        EntityBlueprint checkoutConfig = updated.getResolvedDsl().getEntities().stream()
                .filter(entity -> "checkout-service".equals(entity.getServiceKey()))
                .findFirst()
                .orElseThrow();
        assertNotNull(checkoutConfig);
        assertEquals("zarinpal-default", String.valueOf(updated.getAnswers().get("paymentProvider")));
    }
}
