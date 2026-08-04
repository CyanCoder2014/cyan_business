package com.cyancoder.aiorchestrator.service.impl;

import com.cyancoder.aiorchestrator.domain.*;
import com.cyancoder.aiorchestrator.service.DslValidationService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DefaultDslValidationService implements DslValidationService {
    @Override
    public void validate(PlatformAppDslDefinition dsl, Map<String, Object> platformMetadata) {
        if (dsl == null || dsl.getApp() == null) {
            throw new IllegalArgumentException("DSL app is required");
        }
        if (!StringUtils.hasText(dsl.getApp().getAppKey())) {
            throw new IllegalArgumentException("app.appKey is required");
        }
        if (!StringUtils.hasText(dsl.getApp().getTenantKey())) {
            throw new IllegalArgumentException("app.tenantKey is required");
        }
        if (!StringUtils.hasText(dsl.getApp().getSiteKey())) {
            throw new IllegalArgumentException("app.siteKey is required");
        }

        Set<String> serviceKeys = platformMetadata.keySet().stream()
                .filter(key -> key.endsWith("-service"))
                .collect(Collectors.toSet());
        for (EntityBlueprint entity : dsl.getEntities()) {
            if (!serviceKeys.contains(entity.getServiceKey())) {
                throw new IllegalArgumentException("Unknown serviceKey: " + entity.getServiceKey());
            }
            if (!StringUtils.hasText(entity.getTemplateKey())) {
                throw new IllegalArgumentException("Entity templateKey is required");
            }
            if (!StringUtils.hasText(entity.getEntityKey())) {
                throw new IllegalArgumentException("Entity entityKey is required");
            }
        }

        Set<String> recordKeys = dsl.getEntities().stream()
                .filter(EntityBlueprint::isCreateRecord)
                .map(EntityBlueprint::getRecordKey)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        for (RouteBlueprint route : dsl.getRoutes()) {
            if (!StringUtils.hasText(route.getPath())) {
                throw new IllegalArgumentException("Route path is required");
            }
            if (!StringUtils.hasText(route.getTargetServiceKey()) || !serviceKeys.contains(route.getTargetServiceKey())) {
                throw new IllegalArgumentException("Unknown route target service: " + route.getTargetServiceKey());
            }
            if (!StringUtils.hasText(route.getTargetEntityKey())) {
                throw new IllegalArgumentException("Route target entityKey is required");
            }
            if (StringUtils.hasText(route.getTargetRecordKey()) && !recordKeys.contains(route.getTargetRecordKey())) {
                throw new IllegalArgumentException("Route references unknown recordKey: " + route.getTargetRecordKey());
            }
        }

        for (FlowBlueprint flow : dsl.getFlows()) {
            if (!serviceKeys.contains("bpm-service")) {
                throw new IllegalArgumentException("BPM flows require available service: bpm-service");
            }
            if (!StringUtils.hasText(flow.getFlowKey())) {
                throw new IllegalArgumentException("Flow flowKey is required");
            }
            if (flow.getFlowDefinition() == null || flow.getFlowDefinition().isEmpty()) {
                throw new IllegalArgumentException("Flow definition body is required");
            }
        }

        Map<String, String> resourceOwners = Map.of(
                "PROCESSOR_DEFINITION", "processor-service",
                "AUTOMATION_FLOW", "automation-orchestrator-service",
                "BATCH_DEFINITION", "batch-worker-service"
        );
        for (PlatformResourceBlueprint resource : dsl.getResources()) {
            if (!StringUtils.hasText(resource.getResourceType())
                    || !resourceOwners.containsKey(resource.getResourceType())) {
                throw new IllegalArgumentException("Unsupported resourceType: " + resource.getResourceType());
            }
            String expectedService = resourceOwners.get(resource.getResourceType());
            if (!expectedService.equals(resource.getServiceKey())) {
                throw new IllegalArgumentException(resource.getResourceType()
                        + " must use serviceKey " + expectedService);
            }
            if (!serviceKeys.contains(resource.getServiceKey())) {
                throw new IllegalArgumentException("Resource service is not available: " + resource.getServiceKey());
            }
            if (!StringUtils.hasText(resource.getResourceKey()) || resource.getBody().isEmpty()) {
                throw new IllegalArgumentException("Resource key and body are required");
            }
        }
    }
}
