package com.cyancoder.aiorchestrator.service.impl;

import com.cyancoder.aiorchestrator.api.dto.FollowUpQuestionDto;
import com.cyancoder.aiorchestrator.domain.AppBlueprint;
import com.cyancoder.aiorchestrator.domain.EntityBlueprint;
import com.cyancoder.aiorchestrator.domain.PlatformAppDslDefinition;
import com.cyancoder.aiorchestrator.service.BlueprintCatalogService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class DefaultFollowUpQuestionServiceTest {

    @Test
    void resolvesStructuredFollowUpsForStorefrontProvisioning() {
        DefaultFollowUpQuestionService service = new DefaultFollowUpQuestionService(mock(BlueprintCatalogService.class));

        AppBlueprint blueprint = new AppBlueprint();
        blueprint.setBlueprintKey("ecommerce-crm-zarinpal-v1");
        blueprint.setVersion(1);
        blueprint.setCapabilities(List.of("website", "shop", "checkout"));

        PlatformAppDslDefinition dsl = new PlatformAppDslDefinition();
        dsl.getApp().setCapabilities(List.of("website", "shop", "checkout"));
        dsl.setEntities(List.of(
                entity("content-service", "landing-page"),
                entity("catalog-service", "catalog-product"),
                entity("checkout-service", "checkout-session")
        ));

        List<FollowUpQuestionDto> questions = service.resolveForBlueprint(
                blueprint,
                Map.of("brandName", "Acme"),
                dsl,
                "Create an ecommerce storefront with a domain and checkout"
        );

        assertTrue(questions.stream().anyMatch(question -> "subdomainPrefix".equals(question.key())));
        assertTrue(questions.stream().anyMatch(question -> "starterProductName".equals(question.key())));
        assertTrue(questions.stream().anyMatch(question -> "pageContentSummary".equals(question.key())));
        assertTrue(questions.stream().anyMatch(question -> "paymentProvider".equals(question.key())));
    }

    private EntityBlueprint entity(String serviceKey, String templateKey) {
        EntityBlueprint entity = new EntityBlueprint();
        entity.setServiceKey(serviceKey);
        entity.setTemplateKey(templateKey);
        return entity;
    }
}
