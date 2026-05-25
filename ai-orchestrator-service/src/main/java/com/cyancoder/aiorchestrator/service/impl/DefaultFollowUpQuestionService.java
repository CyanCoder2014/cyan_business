package com.cyancoder.aiorchestrator.service.impl;

import com.cyancoder.aiorchestrator.api.dto.FollowUpQuestionDto;
import com.cyancoder.aiorchestrator.domain.AppBlueprint;
import com.cyancoder.aiorchestrator.domain.BlueprintQuestionDefinition;
import com.cyancoder.aiorchestrator.domain.ClientAppDraft;
import com.cyancoder.aiorchestrator.domain.PlatformAppDslDefinition;
import com.cyancoder.aiorchestrator.service.BlueprintCatalogService;
import com.cyancoder.aiorchestrator.service.FollowUpQuestionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class DefaultFollowUpQuestionService implements FollowUpQuestionService {
    private final BlueprintCatalogService blueprintCatalogService;

    public DefaultFollowUpQuestionService(BlueprintCatalogService blueprintCatalogService) {
        this.blueprintCatalogService = blueprintCatalogService;
    }

    @Override
    public List<FollowUpQuestionDto> resolveForDraft(ClientAppDraft draft) {
        AppBlueprint blueprint = blueprintCatalogService.getActiveByBlueprintKey(draft.getBlueprintKey());
        return resolveForBlueprint(blueprint, draft.getAnswers(), draft.getResolvedDsl(), draft.getLatestIntent());
    }

    @Override
    public List<FollowUpQuestionDto> resolveForBlueprint(AppBlueprint blueprint,
                                                         Map<String, Object> answers,
                                                         PlatformAppDslDefinition dsl,
                                                         String prompt) {
        Map<String, FollowUpQuestionDto> questions = new LinkedHashMap<>();
        for (BlueprintQuestionDefinition question : blueprint.getRequiredQuestions()) {
            if (question.isRequired() && isBlankAnswer(answers, question.getKey())) {
                questions.put(question.getKey(), new FollowUpQuestionDto(
                        question.getKey(),
                        question.getPrompt(),
                        true,
                        "Required by blueprint " + blueprint.getBlueprintKey() + " v" + blueprint.getVersion(),
                        suggestionsFor(question.getKey(), answers)
                ));
            }
        }
        if (needsSubdomainQuestion(blueprint, dsl, answers, prompt)) {
            questions.putIfAbsent("subdomainPrefix", new FollowUpQuestionDto(
                    "subdomainPrefix",
                    "Which subdomain prefix should be used before a custom domain is connected?",
                    true,
                    "Storefront provisioning needs a host decision before routes can be published cleanly.",
                    suggestionsFor("subdomainPrefix", answers)
            ));
        }
        if (needsStarterCatalogQuestion(blueprint, dsl, answers)) {
            questions.putIfAbsent("starterProductName", new FollowUpQuestionDto(
                    "starterProductName",
                    "What product or service should be provisioned first in the catalog?",
                    true,
                    "The selected blueprint provisions a starter catalog item.",
                    suggestionsFor("starterProductName", answers)
            ));
        }
        if (needsPageContentQuestion(blueprint, dsl, answers)) {
            questions.putIfAbsent("pageContentSummary", new FollowUpQuestionDto(
                    "pageContentSummary",
                    "What short homepage copy or hero summary should the first published page use?",
                    true,
                    "The app includes public pages and should not be provisioned with placeholder-only copy.",
                    suggestionsFor("pageContentSummary", answers)
            ));
        }
        if (needsPaymentProviderQuestion(blueprint, dsl, answers)) {
            questions.putIfAbsent("paymentProvider", new FollowUpQuestionDto(
                    "paymentProvider",
                    "Which payment provider should be enabled for the first checkout flow?",
                    true,
                    "Checkout provisioning should align to an explicit provider choice before release.",
                    suggestionsFor("paymentProvider", answers)
            ));
        }
        return new ArrayList<>(questions.values());
    }

    private boolean needsSubdomainQuestion(AppBlueprint blueprint,
                                           PlatformAppDslDefinition dsl,
                                           Map<String, Object> answers,
                                           String prompt) {
        boolean relevant = hasCapability(blueprint, dsl, "website")
                || hasCapability(blueprint, dsl, "shop")
                || mentions(prompt, "domain", "subdomain", "storefront", "site");
        return relevant && isBlankAnswer(answers, "subdomainPrefix") && isBlankAnswer(answers, "desiredDomain");
    }

    private boolean needsStarterCatalogQuestion(AppBlueprint blueprint,
                                                PlatformAppDslDefinition dsl,
                                                Map<String, Object> answers) {
        return hasCapability(blueprint, dsl, "shop")
                && dsl.getEntities().stream().anyMatch(entity -> "catalog-service".equals(entity.getServiceKey()))
                && isBlankAnswer(answers, "starterProductName");
    }

    private boolean needsPageContentQuestion(AppBlueprint blueprint,
                                             PlatformAppDslDefinition dsl,
                                             Map<String, Object> answers) {
        return hasCapability(blueprint, dsl, "website")
                && dsl.getEntities().stream().anyMatch(entity -> "content-service".equals(entity.getServiceKey()))
                && isBlankAnswer(answers, "pageContentSummary");
    }

    private boolean needsPaymentProviderQuestion(AppBlueprint blueprint,
                                                 PlatformAppDslDefinition dsl,
                                                 Map<String, Object> answers) {
        return (hasCapability(blueprint, dsl, "checkout") || hasCapability(blueprint, dsl, "shop"))
                && dsl.getEntities().stream().anyMatch(entity ->
                        "payment-service".equals(entity.getServiceKey()) || "checkout-service".equals(entity.getServiceKey()))
                && isBlankAnswer(answers, "paymentProvider");
    }

    private boolean hasCapability(AppBlueprint blueprint, PlatformAppDslDefinition dsl, String capability) {
        return blueprint.getCapabilities().stream().anyMatch(value -> capability.equalsIgnoreCase(value))
                || dsl.getApp().getCapabilities().stream().anyMatch(value -> capability.equalsIgnoreCase(value));
    }

    private boolean isBlankAnswer(Map<String, Object> answers, String key) {
        Object value = answers == null ? null : answers.get(key);
        return value == null || String.valueOf(value).isBlank();
    }

    private boolean mentions(String prompt, String... tokens) {
        String lower = prompt == null ? "" : prompt.toLowerCase(Locale.ROOT);
        for (String token : tokens) {
            if (lower.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private List<String> suggestionsFor(String key, Map<String, Object> answers) {
        String brand = stringValue(answers == null ? null : answers.get("brandName"));
        if ("subdomainPrefix".equals(key)) {
            return List.of(firstNonBlank(slug(brand), "brand-demo"));
        }
        if ("starterProductName".equals(key)) {
            return List.of(firstNonBlank(brand, "Brand") + " Starter Product");
        }
        if ("pageContentSummary".equals(key)) {
            return List.of("Introduce " + firstNonBlank(brand, "the business") + " and explain the first offer.");
        }
        if ("paymentProvider".equals(key)) {
            return List.of("zarinpal-default", "sep-default", "paypal-default");
        }
        return List.of();
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String slug(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
