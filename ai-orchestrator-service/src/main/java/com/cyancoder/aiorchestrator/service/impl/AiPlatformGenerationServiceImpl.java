package com.cyancoder.aiorchestrator.service.impl;

import com.cyancoder.aiorchestrator.api.dto.GeneratePlatformAppRequest;
import com.cyancoder.aiorchestrator.api.dto.GeneratePlatformAppResponse;
import com.cyancoder.aiorchestrator.api.dto.CreateConversationSessionRequest;
import com.cyancoder.aiorchestrator.api.dto.FollowUpQuestionDto;
import com.cyancoder.aiorchestrator.api.dto.UpdateDraftRequest;
import com.cyancoder.aiorchestrator.api.dto.ProvisioningResultDto;
import com.cyancoder.aiorchestrator.client.LlmClient;
import com.cyancoder.aiorchestrator.client.PlatformMetadataClient;
import com.cyancoder.aiorchestrator.domain.AppBlueprint;
import com.cyancoder.aiorchestrator.domain.ClientAppDraft;
import com.cyancoder.aiorchestrator.domain.PlatformAppDslDefinition;
import com.cyancoder.aiorchestrator.service.AppDraftService;
import com.cyancoder.aiorchestrator.service.AiPlatformGenerationService;
import com.cyancoder.aiorchestrator.service.AiPromptBuilder;
import com.cyancoder.aiorchestrator.service.ConversationSessionService;
import com.cyancoder.aiorchestrator.service.DslValidationService;
import com.cyancoder.aiorchestrator.service.FollowUpQuestionService;
import com.cyancoder.aiorchestrator.service.RetrievalService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    private final FollowUpQuestionService followUpQuestionService;
    private final ConversationSessionService conversationSessionService;

    public AiPlatformGenerationServiceImpl(LlmClient llmClient,
                                           PlatformMetadataClient metadataClient,
                                           RetrievalService retrievalService,
                                           AiPromptBuilder promptBuilder,
                                           DslValidationService dslValidationService,
                                           PlatformProvisioningService provisioningService,
                                           AppDraftService appDraftService,
                                           FollowUpQuestionService followUpQuestionService,
                                           ConversationSessionService conversationSessionService) {
        this.llmClient = llmClient;
        this.metadataClient = metadataClient;
        this.retrievalService = retrievalService;
        this.promptBuilder = promptBuilder;
        this.dslValidationService = dslValidationService;
        this.provisioningService = provisioningService;
        this.appDraftService = appDraftService;
        this.followUpQuestionService = followUpQuestionService;
        this.conversationSessionService = conversationSessionService;
    }

    @Override
    public GeneratePlatformAppResponse generate(GeneratePlatformAppRequest request) {
        var knownDraft = appDraftService.resolveKnownAppDraft(request.appType(), request.tenantKey(), request.siteKey(), request.clientKey(), request.prompt());
        if (knownDraft.isPresent()) {
            ClientAppDraft draft = knownDraft.get();
            if (request.answers() != null && !request.answers().isEmpty()) {
                draft = appDraftService.updateDraft(draft.getDraftId(), new UpdateDraftRequest(
                        request.prompt(),
                        null,
                        request.answers()
                ), "generate-request");
            }
            return resolveKnownDraftResponse(request, draft);
        }
        String tenantKey = defaultScope(request.tenantKey(), "tenant-" + slug(request.prompt()));
        String siteKey = defaultScope(request.siteKey(), "site-" + slug(request.prompt()));
        Map<String, Object> metadata = metadataClient.fetchMetadata(tenantKey, siteKey);
        Map<String, Object> structuredState = new LinkedHashMap<>();
        structuredState.put("tenantKey", tenantKey);
        structuredState.put("siteKey", siteKey);
        structuredState.put("clientKey", request.clientKey());
        if (request.answers() != null) {
            structuredState.putAll(request.answers());
        }
        List<String> retrievedContext = retrievalService.retrieveContext(request.prompt(), structuredState, List.of(), null, null);
        String prompt = promptBuilder.buildPlatformPrompt(request.prompt(), metadata, retrievedContext, tenantKey, siteKey);
        PlatformAppDslDefinition dsl = llmClient.generateDsl(prompt);
        dsl.getApp().setTenantKey(tenantKey);
        dsl.getApp().setSiteKey(siteKey);
        if (dsl.getApp().getCapabilities().isEmpty()) {
            dsl.getApp().setCapabilities(new ArrayList<>(List.of("website")));
        }
        dslValidationService.validate(dsl, metadata);
        List<FollowUpQuestionDto> followUpQuestions = deriveNextQuestions(request, dsl);
        List<String> nextQuestions = followUpQuestions.stream().map(FollowUpQuestionDto::prompt).toList();
        ProvisioningResultDto provisioningResult = request.execute() && nextQuestions.isEmpty()
                ? provisioningService.provision(dsl)
                : null;
        return new GeneratePlatformAppResponse(null, null, dsl, nextQuestions, followUpQuestions, provisioningResult);
    }

    private GeneratePlatformAppResponse resolveKnownDraftResponse(GeneratePlatformAppRequest request, ClientAppDraft draft) {
        PlatformAppDslDefinition dsl = draft.getResolvedDsl();
        List<FollowUpQuestionDto> followUpQuestions = followUpQuestionService.resolveForDraft(draft);
        List<String> nextQuestions = followUpQuestions.stream().map(FollowUpQuestionDto::prompt).toList();
        ProvisioningResultDto provisioningResult = request.execute() && nextQuestions.isEmpty()
                ? provisioningService.provision(dsl)
                : null;
        String sessionId = resolveSessionId(request, draft, nextQuestions.isEmpty());
        return new GeneratePlatformAppResponse(draft.getDraftId(), sessionId, dsl, nextQuestions, followUpQuestions, provisioningResult);
    }

    private List<FollowUpQuestionDto> deriveNextQuestions(GeneratePlatformAppRequest request, PlatformAppDslDefinition dsl) {
        AppBlueprint blueprint = new AppBlueprint();
        blueprint.setBlueprintKey("llm-generated");
        blueprint.setVersion(1);
        blueprint.setCapabilities(new ArrayList<>(dsl.getApp().getCapabilities()));
        return followUpQuestionService.resolveForBlueprint(blueprint, request.answers(), dsl, request.prompt());
    }

    private String resolveSessionId(GeneratePlatformAppRequest request, ClientAppDraft draft, boolean resolved) {
        if (request.sessionId() != null && !request.sessionId().isBlank()) {
            return request.sessionId();
        }
        if (resolved) {
            return null;
        }
        return conversationSessionService.createSession(new CreateConversationSessionRequest(
                "PANEL",
                draft.getTenantKey(),
                draft.getSiteKey(),
                draft.getClientKey(),
                draft.getDraftId(),
                draft.getAppType(),
                draft.getTitle(),
                draft.getAnswers()
        )).getSessionId();
    }

    private String defaultScope(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String slug(String prompt) {
        return prompt.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
