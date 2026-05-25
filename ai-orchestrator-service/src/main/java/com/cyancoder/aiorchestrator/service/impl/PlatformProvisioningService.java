package com.cyancoder.aiorchestrator.service.impl;

import com.cyancoder.aiorchestrator.api.dto.ProvisioningResultDto;
import com.cyancoder.aiorchestrator.client.PlatformProvisioningClient;
import com.cyancoder.aiorchestrator.domain.EntityBlueprint;
import com.cyancoder.aiorchestrator.domain.ProvisioningRun;
import com.cyancoder.aiorchestrator.domain.ProvisioningRunStatus;
import com.cyancoder.aiorchestrator.domain.ProvisioningStepResult;
import com.cyancoder.aiorchestrator.domain.PlatformAppDslDefinition;
import com.cyancoder.aiorchestrator.domain.RouteBlueprint;
import com.cyancoder.aiorchestrator.repo.ProvisioningRunRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlatformProvisioningService {
    private final PlatformProvisioningClient provisioningClient;
    private final ProvisioningRunRepository provisioningRunRepository;

    public PlatformProvisioningService(PlatformProvisioningClient provisioningClient,
                                       ProvisioningRunRepository provisioningRunRepository) {
        this.provisioningClient = provisioningClient;
        this.provisioningRunRepository = provisioningRunRepository;
    }

    public ProvisioningResultDto provision(PlatformAppDslDefinition dsl) {
        return provision(null, dsl, null, null);
    }

    public ProvisioningResultDto provision(String draftId, PlatformAppDslDefinition dsl, ProvisioningRun run, String requestIdempotencyKey) {
        String tenantKey = dsl.getApp().getTenantKey();
        String siteKey = dsl.getApp().getSiteKey();
        List<Map<String, Object>> createdDefinitions = new ArrayList<>();
        List<Map<String, Object>> createdRecords = new ArrayList<>();
        List<Map<String, Object>> createdFlows = new ArrayList<>();
        List<Map<String, Object>> deliveryEndpoints = new ArrayList<>();

        ensureThemeExists(draftId, tenantKey, siteKey, createdDefinitions, createdRecords, run, requestIdempotencyKey);

        for (EntityBlueprint entity : dsl.getEntities()) {
            if (entity.isCreateDefinition()) {
                createdDefinitions.add(recordStep(
                        run,
                        stepKey("definition", entity.getServiceKey(), entity.getEntityKey()),
                        entity.getServiceKey(),
                        "/internal/entities/templates/" + entity.getTemplateKey() + "/definitions",
                        buildIdempotencyKey(requestIdempotencyKey, draftId, entity.getServiceKey(), entity.getEntityKey(), "definition"),
                        () -> provisioningClient.createDefinitionFromTemplate(entity.getServiceKey(), entity.getTemplateKey(), entity.getEntityKey(), tenantKey, siteKey)
                ));
            }
            if (entity.isCreateRecord()) {
                createdRecords.add(recordStep(
                        run,
                        stepKey("record", entity.getServiceKey(), entity.getRecordKey()),
                        entity.getServiceKey(),
                        "/internal/entities/records/" + entity.getEntityKey(),
                        buildIdempotencyKey(requestIdempotencyKey, draftId, entity.getServiceKey(), entity.getRecordKey(), "record"),
                        () -> provisioningClient.createRecord(entity.getServiceKey(), entity.getEntityKey(), entity.getRecordKey(), entity.getRecordData(), tenantKey, siteKey)
                ));
            }
        }

        if (!dsl.getRoutes().isEmpty()) {
            recordStep(
                    run,
                    stepKey("definition", "storefront-service", "site-route"),
                    "storefront-service",
                    "/internal/entities/templates/site-route/definitions",
                    buildIdempotencyKey(requestIdempotencyKey, draftId, "storefront-service", "site-route", "definition"),
                    () -> provisioningClient.createDefinitionFromTemplate("storefront-service", "site-route", "site-route", tenantKey, siteKey)
            );
            for (RouteBlueprint route : dsl.getRoutes()) {
                Map<String, Object> routeData = new LinkedHashMap<>();
                routeData.put("path", route.getPath());
                routeData.put("status", "PUBLISHED");
                routeData.put("routeType", route.getPageType() == null ? "PAGE" : route.getPageType());
                routeData.put("indexable", true);
                routeData.put("entityRef", Map.of(
                        "service", route.getTargetServiceKey(),
                        "entityKey", route.getTargetEntityKey(),
                        "recordKey", route.getTargetRecordKey()
                ));
                routeData.put("themeRef", Map.of("recordKey", route.getThemeRecordKey() == null ? "theme-main" : route.getThemeRecordKey()));
                createdRecords.add(recordStep(
                        run,
                        stepKey("route", "storefront-service", route.getRouteKey()),
                        "storefront-service",
                        "/internal/entities/records/site-route",
                        buildIdempotencyKey(requestIdempotencyKey, draftId, "storefront-service", route.getRouteKey(), "route"),
                        () -> provisioningClient.createRecord("storefront-service", "site-route", route.getRouteKey(), routeData, tenantKey, siteKey)
                ));
            }
        }

        for (var flow : dsl.getFlows()) {
            createdFlows.add(recordStep(
                    run,
                    stepKey("flow", "bpm-service", flow.getFlowKey()),
                    "bpm-service",
                    "/internal/bpm/flows",
                    buildIdempotencyKey(requestIdempotencyKey, draftId, "bpm-service", flow.getFlowKey(), "flow"),
                    () -> provisioningClient.createBpmFlow(flow.getFlowDefinition(), tenantKey, siteKey)
            ));
        }

        for (String api : dsl.getDelivery().getPublicApis()) {
            deliveryEndpoints.add(Map.of("type", "public", "path", api));
        }
        for (String api : dsl.getDelivery().getBotApis()) {
            deliveryEndpoints.add(Map.of("type", "bot", "path", api));
        }
        deliveryEndpoints.add(Map.of("type", "public", "path", "/public/storefront/render?path=/"));
        deliveryEndpoints.add(Map.of("type", "public", "path", "/public/storefront/sitemap"));

        if (run != null) {
            run.setStatus(ProvisioningRunStatus.SUCCESS);
            run.setFinishedAt(Instant.now());
            provisioningRunRepository.save(run);
        }

        return new ProvisioningResultDto("PROVISIONED", createdDefinitions, createdRecords, createdFlows, deliveryEndpoints, dsl.getManualActions());
    }

    private void ensureThemeExists(String draftId,
                                   String tenantKey,
                                   String siteKey,
                                   List<Map<String, Object>> createdDefinitions,
                                   List<Map<String, Object>> createdRecords,
                                   ProvisioningRun run,
                                   String requestIdempotencyKey) {
        createdDefinitions.add(recordStep(
                run,
                stepKey("definition", "storefront-service", "theme-layout"),
                "storefront-service",
                "/internal/entities/templates/theme-layout/definitions",
                buildIdempotencyKey(requestIdempotencyKey, draftId, "storefront-service", "theme-layout", "definition"),
                () -> provisioningClient.createDefinitionFromTemplate("storefront-service", "theme-layout", "theme-layout", tenantKey, siteKey)
        ));
        createdRecords.add(recordStep(
                run,
                stepKey("record", "storefront-service", "theme-main"),
                "storefront-service",
                "/internal/entities/records/theme-layout",
                buildIdempotencyKey(requestIdempotencyKey, draftId, "storefront-service", "theme-main", "record"),
                () -> provisioningClient.createRecord("storefront-service", "theme-layout", "theme-main", Map.of(
                        "themeKey", "theme-main",
                        "brandName", dslBrandName(draftId, tenantKey, siteKey),
                        "status", "ACTIVE",
                        "templateKey", "default-theme",
                        "themeCategory", "SITE",
                        "previewImage", "",
                        "navigation", List.of(
                                Map.of("label", "Home", "path", "/", "children", List.of()),
                                Map.of("label", "About", "path", "/about", "children", List.of())
                        ),
                        "globalSeo", Map.of(
                                "siteName", dslBrandName(draftId, tenantKey, siteKey),
                                "defaultTitleTemplate", dslBrandName(draftId, tenantKey, siteKey) + " | %s",
                                "defaultDescription", "Provisioned storefront theme",
                                "organizationJsonLd", ""
                        ),
                        "blocks", List.of(
                                Map.of("blockKey", "hero", "componentType", "hero-banner", "props", Map.of())
                        )
                ), tenantKey, siteKey)
        ));
    }

    private String dslBrandName(String draftId, String tenantKey, String siteKey) {
        if (siteKey != null && !siteKey.isBlank()) {
            return siteKey;
        }
        if (tenantKey != null && !tenantKey.isBlank()) {
            return tenantKey;
        }
        return draftId == null || draftId.isBlank() ? "Default Theme" : draftId;
    }

    private Map<String, Object> recordStep(ProvisioningRun run,
                                           String stepKey,
                                           String serviceKey,
                                           String endpointPath,
                                           String idempotencyKey,
                                           StepAction action) {
        try {
            Map<String, Object> response = action.execute();
            if (run != null) {
                upsertStep(run, stepKey, serviceKey, endpointPath, "SUCCESS", idempotencyKey, summarize(response), response);
            }
            return response;
        } catch (RuntimeException ex) {
            if (run != null) {
                upsertStep(run, stepKey, serviceKey, endpointPath, "FAILED", idempotencyKey, ex.getMessage(), Map.of());
                run.setStatus(ProvisioningRunStatus.FAILED);
                run.setFinishedAt(Instant.now());
                provisioningRunRepository.save(run);
            }
            throw ex;
        }
    }

    private void upsertStep(ProvisioningRun run,
                            String stepKey,
                            String serviceKey,
                            String endpointPath,
                            String status,
                            String idempotencyKey,
                            String summary,
                            Map<String, Object> response) {
        ProvisioningStepResult step = run.getStepResults().stream()
                .filter(item -> stepKey.equals(item.getStepKey()))
                .findFirst()
                .orElseGet(() -> {
                    ProvisioningStepResult created = new ProvisioningStepResult();
                    created.setStepKey(stepKey);
                    run.getStepResults().add(created);
                    return created;
                });
        step.setServiceKey(serviceKey);
        step.setEndpointPath(endpointPath);
        step.setStatus(status);
        step.setIdempotencyKey(idempotencyKey);
        step.setSummary(summary);
        step.setResponse(response);
        provisioningRunRepository.save(run);
    }

    private String buildIdempotencyKey(String requestIdempotencyKey, String draftId, String serviceKey, String subjectKey, String operation) {
        String base = requestIdempotencyKey == null || requestIdempotencyKey.isBlank()
                ? (draftId == null ? "adhoc" : draftId)
                : requestIdempotencyKey;
        return base + ":" + serviceKey + ":" + subjectKey + ":" + operation;
    }

    private String stepKey(String kind, String serviceKey, String subjectKey) {
        return kind + ":" + serviceKey + ":" + subjectKey;
    }

    private String summarize(Map<String, Object> response) {
        if (response == null || response.isEmpty()) {
            return "ok";
        }
        return response.keySet().stream().limit(4).reduce((left, right) -> left + "," + right).orElse("ok");
    }

    @FunctionalInterface
    private interface StepAction {
        Map<String, Object> execute();
    }
}
