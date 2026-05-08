package com.cyancoder.aiorchestrator.service.impl;

import com.cyancoder.aiorchestrator.api.dto.ProvisioningResultDto;
import com.cyancoder.aiorchestrator.client.PlatformProvisioningClient;
import com.cyancoder.aiorchestrator.domain.EntityBlueprint;
import com.cyancoder.aiorchestrator.domain.PlatformAppDslDefinition;
import com.cyancoder.aiorchestrator.domain.RouteBlueprint;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlatformProvisioningService {
    private final PlatformProvisioningClient provisioningClient;

    public PlatformProvisioningService(PlatformProvisioningClient provisioningClient) {
        this.provisioningClient = provisioningClient;
    }

    public ProvisioningResultDto provision(PlatformAppDslDefinition dsl) {
        String tenantKey = dsl.getApp().getTenantKey();
        String siteKey = dsl.getApp().getSiteKey();
        List<Map<String, Object>> createdDefinitions = new ArrayList<>();
        List<Map<String, Object>> createdRecords = new ArrayList<>();
        List<Map<String, Object>> createdFlows = new ArrayList<>();
        List<Map<String, Object>> deliveryEndpoints = new ArrayList<>();

        ensureThemeExists(tenantKey, siteKey, createdDefinitions, createdRecords);

        for (EntityBlueprint entity : dsl.getEntities()) {
            if (entity.isCreateDefinition()) {
                createdDefinitions.add(provisioningClient.createDefinitionFromTemplate(entity.getServiceKey(), entity.getTemplateKey(), entity.getEntityKey(), tenantKey, siteKey));
            }
            if (entity.isCreateRecord()) {
                createdRecords.add(provisioningClient.createRecord(entity.getServiceKey(), entity.getEntityKey(), entity.getRecordKey(), entity.getRecordData(), tenantKey, siteKey));
            }
        }

        if (!dsl.getRoutes().isEmpty()) {
            provisioningClient.createDefinitionFromTemplate("storefront-service", "site-route", "site-route", tenantKey, siteKey);
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
                createdRecords.add(provisioningClient.createRecord("storefront-service", "site-route", route.getRouteKey(), routeData, tenantKey, siteKey));
            }
        }

        for (var flow : dsl.getFlows()) {
            createdFlows.add(provisioningClient.createBpmFlow(flow.getFlowDefinition(), tenantKey, siteKey));
        }

        for (String api : dsl.getDelivery().getPublicApis()) {
            deliveryEndpoints.add(Map.of("type", "public", "path", api));
        }
        for (String api : dsl.getDelivery().getBotApis()) {
            deliveryEndpoints.add(Map.of("type", "bot", "path", api));
        }
        deliveryEndpoints.add(Map.of("type", "public", "path", "/public/storefront/render?path=/"));
        deliveryEndpoints.add(Map.of("type", "public", "path", "/public/storefront/sitemap"));

        return new ProvisioningResultDto("PROVISIONED", createdDefinitions, createdRecords, createdFlows, deliveryEndpoints, dsl.getManualActions());
    }

    private void ensureThemeExists(String tenantKey, String siteKey, List<Map<String, Object>> createdDefinitions, List<Map<String, Object>> createdRecords) {
        createdDefinitions.add(provisioningClient.createDefinitionFromTemplate("storefront-service", "theme-layout", "theme-layout", tenantKey, siteKey));
        createdRecords.add(provisioningClient.createRecord("storefront-service", "theme-layout", "theme-main", Map.of(
                "name", "Default Theme",
                "status", "ACTIVE",
                "layoutType", "site",
                "tokens", Map.of("brandColor", "#0f766e", "surfaceColor", "#ffffff")
        ), tenantKey, siteKey));
    }
}

