package com.cyancoder.aiorchestrator.service.impl;

import com.cyancoder.aiorchestrator.api.dto.GeneratePlatformAppRequest;
import com.cyancoder.aiorchestrator.api.dto.GeneratePlatformAppResponse;
import com.cyancoder.aiorchestrator.api.dto.ProvisioningResultDto;
import com.cyancoder.aiorchestrator.client.LlmClient;
import com.cyancoder.aiorchestrator.client.PlatformMetadataClient;
import com.cyancoder.aiorchestrator.domain.ClientAppDraft;
import com.cyancoder.aiorchestrator.domain.PlatformAppDslDefinition;
import com.cyancoder.aiorchestrator.service.AppDraftService;
import com.cyancoder.aiorchestrator.service.AiPlatformGenerationService;
import com.cyancoder.aiorchestrator.service.AiPromptBuilder;
import com.cyancoder.aiorchestrator.service.DslValidationService;
import com.cyancoder.aiorchestrator.service.RetrievalService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AiPlatformGenerationServiceImpl implements AiPlatformGenerationService {
    private final LlmClient llmClient;
    private final PlatformMetadataClient metadataClient;
    private final RetrievalService retrievalService;
    private final AiPromptBuilder promptBuilder;
    private final DslValidationService dslValidationService;
    private final PlatformProvisioningService provisioningService;
    private final AppDraftService appDraftService;

    public AiPlatformGenerationServiceImpl(LlmClient llmClient,
                                           PlatformMetadataClient metadataClient,
                                           RetrievalService retrievalService,
                                           AiPromptBuilder promptBuilder,
                                           DslValidationService dslValidationService,
                                           PlatformProvisioningService provisioningService,
                                           AppDraftService appDraftService) {
        this.llmClient = llmClient;
        this.metadataClient = metadataClient;
        this.retrievalService = retrievalService;
        this.promptBuilder = promptBuilder;
        this.dslValidationService = dslValidationService;
        this.provisioningService = provisioningService;
        this.appDraftService = appDraftService;
    }

    @Override
    public GeneratePlatformAppResponse generate(GeneratePlatformAppRequest request) {
        var knownDraft = appDraftService.resolveKnownAppDraft(request.appType(), request.tenantKey(), request.siteKey(), request.clientKey(), request.prompt());
        if (knownDraft.isPresent()) {
            return resolveKnownDraftResponse(request, knownDraft.get());
        }
        String tenantKey = defaultScope(request.tenantKey(), "tenant-" + slug(request.prompt()));
        String siteKey = defaultScope(request.siteKey(), "site-" + slug(request.prompt()));
        Map<String, Object> metadata = metadataClient.fetchMetadata(tenantKey, siteKey);
        List<String> retrievedContext = retrievalService.retrieveContext(request.prompt());
        String prompt = promptBuilder.buildPlatformPrompt(request.prompt(), metadata, retrievedContext, tenantKey, siteKey);
        PlatformAppDslDefinition dsl = llmClient.generateDsl(prompt);
        dsl.getApp().setTenantKey(tenantKey);
        dsl.getApp().setSiteKey(siteKey);
        if (dsl.getApp().getCapabilities().isEmpty()) {
            dsl.getApp().setCapabilities(new ArrayList<>(List.of("website")));
        }
        dslValidationService.validate(dsl, metadata);
        List<String> nextQuestions = deriveNextQuestions(request, dsl);
        ProvisioningResultDto provisioningResult = request.execute() && nextQuestions.isEmpty()
                ? provisioningService.provision(dsl)
                : null;
        return new GeneratePlatformAppResponse(null, dsl, nextQuestions, provisioningResult);
    }

    private GeneratePlatformAppResponse resolveKnownDraftResponse(GeneratePlatformAppRequest request, ClientAppDraft draft) {
        PlatformAppDslDefinition dsl = draft.getResolvedDsl();
        List<String> nextQuestions = draft.getPendingQuestions() == null ? List.of() : draft.getPendingQuestions();
        ProvisioningResultDto provisioningResult = request.execute() && nextQuestions.isEmpty()
                ? provisioningService.provision(dsl)
                : null;
        return new GeneratePlatformAppResponse(draft.getDraftId(), dsl, nextQuestions, provisioningResult);
    }

    private List<String> deriveNextQuestions(GeneratePlatformAppRequest request, PlatformAppDslDefinition dsl) {
        List<String> questions = new ArrayList<>();
        if (dsl.getApp().getDesiredDomain() == null && request.prompt().toLowerCase().contains("domain")) {
            questions.add("Which domain should be connected to this app?");
        }
        if (dsl.getApp().getCapabilities().contains("shop") && dsl.getEntities().stream().noneMatch(entity -> "catalog-service".equals(entity.getServiceKey()))) {
            questions.add("What products or services should the initial shop catalog contain?");
        }
        if (dsl.getApp().getCapabilities().contains("website") && dsl.getRoutes().isEmpty()) {
            questions.add("Which public pages should be created first?");
        }
        return questions;
    }

    private String defaultScope(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String slug(String prompt) {
        return prompt.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
