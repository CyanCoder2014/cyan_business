package com.cyancoder.aiorchestrator.client.impl;

import com.cyancoder.aiorchestrator.client.LlmClient;
import com.cyancoder.aiorchestrator.domain.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class HeuristicLlmClient implements LlmClient {
    @Override
    public PlatformAppDslDefinition generateDsl(String prompt) {
        String lower = prompt.toLowerCase(Locale.ROOT);
        PlatformAppDslDefinition dsl = new PlatformAppDslDefinition();
        AppDescriptor app = new AppDescriptor();
        app.setAppKey(slug(prompt));
        app.setTitle(deriveTitle(prompt));
        app.setType(resolveType(lower));
        app.setCapabilities(resolveCapabilities(lower));
        dsl.setApp(app);

        List<EntityBlueprint> entities = new ArrayList<>();
        entities.add(contentEntity("landing-page", "home-page", Map.of(
                "title", deriveTitle(prompt),
                "slug", "/",
                "status", "PUBLISHED",
                "contentType", "landing-page",
                "body", "AI-generated homepage structure"
        )));
        if (lower.contains("blog")) {
            entities.add(contentEntity("blog-page", "blog-index", Map.of(
                    "title", "Blog",
                    "slug", "/blog",
                    "status", "PUBLISHED",
                    "contentType", "blog-page",
                    "body", "AI-generated blog landing page"
            )));
        }
        if (lower.contains("shop") || lower.contains("product") || lower.contains("sell")) {
            EntityBlueprint product = new EntityBlueprint();
            product.setServiceKey("catalog-service");
            product.setTemplateKey("catalog-product");
            product.setEntityKey("shop-product");
            product.setRecordKey("starter-product");
            product.setCreateRecord(true);
            product.setRecordData(Map.of(
                    "name", "Starter Product",
                    "sku", "STARTER-001",
                    "status", "ACTIVE",
                    "price", 1000000,
                    "currency", "IRR"
            ));
            entities.add(product);
        }
        if (lower.contains("crm") || lower.contains("lead") || lower.contains("contact")) {
            EntityBlueprint lead = new EntityBlueprint();
            lead.setServiceKey("crm-service");
            lead.setTemplateKey("crm-lead");
            lead.setEntityKey("crm-lead");
            lead.setCreateDefinition(true);
            entities.add(lead);
        }
        dsl.setEntities(entities);

        List<RouteBlueprint> routes = new ArrayList<>();
        routes.add(route("home-route", "/", "content-service", "landing-page", "home-page"));
        if (lower.contains("blog")) {
            routes.add(route("blog-route", "/blog", "content-service", "blog-page", "blog-index"));
        }
        if (lower.contains("shop") || lower.contains("product") || lower.contains("sell")) {
            routes.add(route("product-route", "/products/starter-product", "catalog-service", "shop-product", "starter-product"));
        }
        dsl.setRoutes(routes);

        if (lower.contains("approval") || lower.contains("bpm") || lower.contains("review")) {
            FlowBlueprint flowBlueprint = new FlowBlueprint();
            flowBlueprint.setFlowKey("orchestrated-order-review");
            flowBlueprint.setFlowDefinition(Map.of(
                    "flowKey", "orchestrated-order-review",
                    "name", "Orchestrated Order Review",
                    "startState", "draft-order",
                    "active", true,
                    "states", List.of(
                            Map.of("id", "draft-order", "displayName", "Draft Order", "terminal", false, "entityService", "commerce-service", "entityKey", "shop-order", "formKey", "shop-order", "submitMode", "DYNAMIC"),
                            Map.of("id", "approved-order", "displayName", "Approved Order", "terminal", true)
                    ),
                    "transitions", List.of(
                            Map.of("id", "approve", "fromState", "draft-order", "toState", "approved-order", "label", "Approve", "allowedRoles", List.of("ROLE_ADMIN"))
                    )
            ));
            dsl.setFlows(List.of(flowBlueprint));
        }

        DeliveryBlueprint delivery = new DeliveryBlueprint();
        delivery.setPublicApis(List.of("/public/storefront/render?path=/", "/public/storefront/sitemap"));
        delivery.setBotApis(List.of("/api/content-service/**", "/api/catalog-service/**", "/api/crm-service/**", "/api/bpm-service/**"));
        dsl.setDelivery(delivery);
        if (lower.contains("domain")) {
            dsl.setManualActions(List.of("Domain purchase and DNS connection require external registrar integration."));
        }
        return dsl;
    }

    private EntityBlueprint contentEntity(String templateKey, String recordKey, Map<String, Object> data) {
        EntityBlueprint entity = new EntityBlueprint();
        entity.setServiceKey("content-service");
        entity.setTemplateKey(templateKey);
        entity.setEntityKey(templateKey);
        entity.setRecordKey(recordKey);
        entity.setCreateRecord(true);
        entity.setRecordData(data);
        return entity;
    }

    private RouteBlueprint route(String routeKey, String path, String targetService, String targetEntityKey, String targetRecordKey) {
        RouteBlueprint route = new RouteBlueprint();
        route.setRouteKey(routeKey);
        route.setPath(path);
        route.setTargetServiceKey(targetService);
        route.setTargetEntityKey(targetEntityKey);
        route.setTargetRecordKey(targetRecordKey);
        route.setThemeRecordKey("theme-main");
        route.setPageType("PAGE");
        return route;
    }

    private PlatformAppType resolveType(String lower) {
        if (lower.contains("shop") || lower.contains("e-commerce") || lower.contains("ecommerce")) {
            return PlatformAppType.SHOP;
        }
        if (lower.contains("crm")) {
            return PlatformAppType.CRM;
        }
        if (lower.contains("blog")) {
            return PlatformAppType.BLOG;
        }
        if (lower.contains("bpm")) {
            return PlatformAppType.BPM_PORTAL;
        }
        return PlatformAppType.WEBSITE;
    }

    private List<String> resolveCapabilities(String lower) {
        List<String> capabilities = new ArrayList<>();
        capabilities.add("website");
        if (lower.contains("blog")) capabilities.add("blog");
        if (lower.contains("shop") || lower.contains("product")) capabilities.add("shop");
        if (lower.contains("crm") || lower.contains("lead")) capabilities.add("crm");
        if (lower.contains("bpm") || lower.contains("review")) capabilities.add("bpm");
        return capabilities;
    }

    private String deriveTitle(String prompt) {
        String slug = slug(prompt).replace('-', ' ').trim();
        if (slug.isBlank()) {
            return "Generated App";
        }
        return Character.toUpperCase(slug.charAt(0)) + slug.substring(1);
    }

    private String slug(String prompt) {
        return prompt.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}

