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
import com.cyancoder.aiorchestrator.service.BillingUsageReporter;
import com.cyancoder.aiorchestrator.service.ConversationSessionService;
import com.cyancoder.aiorchestrator.service.DslValidationService;
import com.cyancoder.aiorchestrator.service.FollowUpQuestionService;
import com.cyancoder.aiorchestrator.service.RetrievalService;
import com.cyancoder.aiorchestrator.service.ServiceAvailabilityResolver;
import com.cyancoder.aiorchestrator.service.ServiceAvailabilitySnapshot;
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
    private final ServiceAvailabilityResolver availabilityResolver;
    private final BillingUsageReporter usageReporter;

    public AiPlatformGenerationServiceImpl(LlmClient llmClient,
                                           PlatformMetadataClient metadataClient,
                                           RetrievalService retrievalService,
                                           AiPromptBuilder promptBuilder,
                                           DslValidationService dslValidationService,
                                           PlatformProvisioningService provisioningService,
                                           AppDraftService appDraftService,
                                           FollowUpQuestionService followUpQuestionService,
                                           ConversationSessionService conversationSessionService,
                                           ServiceAvailabilityResolver availabilityResolver,
                                           BillingUsageReporter usageReporter) {
        this.llmClient = llmClient;
        this.metadataClient = metadataClient;
        this.retrievalService = retrievalService;
        this.promptBuilder = promptBuilder;
        this.dslValidationService = dslValidationService;
        this.provisioningService = provisioningService;
        this.appDraftService = appDraftService;
        this.followUpQuestionService = followUpQuestionService;
        this.conversationSessionService = conversationSessionService;
        this.availabilityResolver = availabilityResolver;
        this.usageReporter = usageReporter;
    }

    @Override
    public GeneratePlatformAppResponse generate(GeneratePlatformAppRequest request) {
        ServiceAvailabilitySnapshot availability = availabilityResolver.resolve(request.availableServiceKeys());
        var knownDraft = appDraftService.resolveKnownAppDraft(request.appType(), request.tenantKey(),
                request.siteKey(), request.clientKey(), request.prompt(), availability.availableServiceKeys());
        if (knownDraft.isPresent()) {
            ClientAppDraft draft = knownDraft.get();
            if ((request.answers() != null && !request.answers().isEmpty())
                    || (request.availableServiceKeys() != null && !request.availableServiceKeys().isEmpty())) {
                draft = appDraftService.updateDraft(draft.getDraftId(), new UpdateDraftRequest(
                        request.prompt(),
                        null,
                        request.answers(),
                        availability.availableServiceKeys()
                ), "generate-request");
            }
            return resolveKnownDraftResponse(request, draft);
        }
        String tenantKey = defaultScope(request.tenantKey(), "tenant-" + slug(request.prompt()));
        String siteKey = defaultScope(request.siteKey(), "site-" + slug(request.prompt()));
        Map<String, Object> metadata = metadataClient.fetchMetadata(tenantKey, siteKey, availability);
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
        dsl.getApp().setAvailableServiceKeys(availability.availableServiceKeys());
        removeUnavailablePlanItems(dsl, availability.availableServiceKeys());
        if (dsl.getApp().getCapabilities().isEmpty()) {
            dsl.getApp().setCapabilities(new ArrayList<>(List.of("website")));
        }
        dslValidationService.validate(dsl, metadata);
        List<FollowUpQuestionDto> followUpQuestions = deriveNextQuestions(request, dsl);
        List<String> nextQuestions = followUpQuestions.stream().map(FollowUpQuestionDto::prompt).toList();
        ProvisioningResultDto provisioningResult = request.execute() && nextQuestions.isEmpty()
                ? provisioningService.provision(dsl)
                : null;
        usageReporter.increment(tenantKey, "aiGenerations");
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
            conversationSessionService.linkDraft(request.sessionId(), draft.getDraftId());
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
                draft.getAnswers(),
                draft.getAvailableServiceKeys()
        )).getSessionId();
    }

    private String defaultScope(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private void removeUnavailablePlanItems(PlatformAppDslDefinition dsl, List<String> services) {
        var available = new java.util.LinkedHashSet<>(services);
        if (available.isEmpty()) return;
        List<String> omitted = new ArrayList<>();
        dsl.getEntities().removeIf(entity -> {
            boolean remove = !available.contains(entity.getServiceKey());
            if (remove) omitted.add(entity.getServiceKey() + " entity " + entity.getEntityKey());
            return remove;
        });
        dsl.getRoutes().removeIf(route -> {
            boolean remove = !available.contains("storefront-service")
                    || !available.contains(route.getTargetServiceKey());
            if (remove) omitted.add("storefront route " + route.getRouteKey());
            return remove;
        });
        if (!available.contains("bpm-service") && !dsl.getFlows().isEmpty()) {
            omitted.add("BPM flows");
            dsl.getFlows().clear();
        }
        dsl.getResources().removeIf(resource -> {
            boolean remove = !available.contains(resource.getServiceKey());
            if (remove) omitted.add(resource.getResourceType() + " " + resource.getResourceKey());
            return remove;
        });
        omitted.forEach(item -> dsl.getManualActions().add(
                "Skipped because its owning microservice is unavailable: " + item));
    }

    private String slug(String prompt) {
        return prompt.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
