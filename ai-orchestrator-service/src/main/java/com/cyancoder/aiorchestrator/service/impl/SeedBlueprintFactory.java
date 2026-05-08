package com.cyancoder.aiorchestrator.service.impl;

import com.cyancoder.aiorchestrator.domain.AppBlueprint;
import com.cyancoder.aiorchestrator.domain.BlueprintQuestionDefinition;
import com.cyancoder.aiorchestrator.domain.DeliveryBlueprint;
import com.cyancoder.aiorchestrator.domain.EntityBlueprint;
import com.cyancoder.aiorchestrator.domain.FlowBlueprint;
import com.cyancoder.aiorchestrator.domain.PlatformAppDslDefinition;
import com.cyancoder.aiorchestrator.domain.PlatformAppType;
import com.cyancoder.aiorchestrator.domain.RouteBlueprint;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SeedBlueprintFactory {
    public List<AppBlueprint> createDefaults() {
        return List.of(
                blueprint("personal-site-v1", "personal-site", "Personal Site", "Simple personal landing site with biography and contact routes.",
                        List.of("website"),
                        List.of(
                                question("brandName", "What name should appear on the site?", true),
                                question("homePageTitle", "What should the homepage title be?", true),
                                question("aboutPageTitle", "What should the about page title be?", true)
                        ),
                        Map.of("aboutPageTitle", "About Me"),
                        personalSiteDsl()),
                blueprint("company-site-v1", "company-site", "Company Site", "Corporate marketing site with landing and company pages.",
                        List.of("website"),
                        List.of(
                                question("brandName", "What is the company or brand name?", true),
                                question("homePageTitle", "What should the homepage headline be?", true),
                                question("aboutPageTitle", "What should the about page title be?", true)
                        ),
                        Map.of("aboutPageTitle", "About Us"),
                        companySiteDsl()),
                blueprint("blog-basic-v1", "blog", "Blog Site", "Landing page plus blog structure.",
                        List.of("website", "blog"),
                        List.of(
                                question("brandName", "What brand name should appear on the site?", true),
                                question("homePageTitle", "What should the homepage title be?", true),
                                question("blogSlug", "What slug should be used for the blog index?", true)
                        ),
                        Map.of("blogSlug", "blog"),
                        blogDsl()),
                blueprint("ecommerce-crm-zarinpal-v1", "e-commerce", "E-Commerce + CRM", "Storefront, catalog, CRM, checkout, notifications, and payment-oriented starter app.",
                        List.of("website", "shop", "crm", "notification", "checkout"),
                        List.of(
                                question("brandName", "What brand name should be shown for the shop?", true),
                                question("homePageTitle", "What is the storefront homepage title?", true),
                                question("starterProductName", "What starter product should be created first?", true),
                                question("starterProductSku", "What SKU should the first product use?", true),
                                question("subdomainPrefix", "What subdomain prefix should be used if a subdomain is chosen?", true),
                                question("paymentProvider", "Which payment provider should be preferred?", true)
                        ),
                        Map.of(
                                "starterProductSku", "STARTER-001",
                                "paymentProvider", "zarinpal-default",
                                "blogSlug", "blog",
                                "subdomainPrefix", "shop-demo"
                        ),
                        ecommerceDsl()),
                blueprint("crm-basic-v1", "crm", "CRM", "Lead and contact management starter app.",
                        List.of("crm"),
                        List.of(
                                question("brandName", "What business name should be used for the CRM?", true),
                                question("leadEntityKey", "What entity key should be used for leads?", true)
                        ),
                        Map.of("leadEntityKey", "crm-lead"),
                        crmDsl()),
                blueprint("invoice-management-v1", "invoice-management", "Invoice Management", "Invoice, payment tracking, and notification starter app.",
                        List.of("commerce", "finance", "notification"),
                        List.of(
                                question("brandName", "What business name should be used?", true),
                                question("defaultAccountKey", "What account key should finance transactions use?", true)
                        ),
                        Map.of("defaultAccountKey", "main-account"),
                        invoiceDsl()),
                blueprint("erp-basic-v1", "erp", "ERP Starter", "Orders, invoices, stock, finance, reporting, and workflow starter.",
                        List.of("commerce", "finance", "inventory", "report", "bpm"),
                        List.of(
                                question("brandName", "What business name should be used?", true),
                                question("warehouseKey", "What warehouse key should inventory use?", true),
                                question("defaultAccountKey", "What account key should finance transactions use?", true)
                        ),
                        Map.of("warehouseKey", "main-warehouse", "defaultAccountKey", "main-account"),
                        erpDsl()),
                blueprint("automation-basic-v1", "automation", "Automation Starter", "CRM, reporting, notifications, and BPM-assisted automation starter.",
                        List.of("crm", "notification", "report", "bpm"),
                        List.of(
                                question("brandName", "What business name should be used?", true),
                                question("notificationEmail", "What email should receive automation notifications?", true)
                        ),
                        Map.of("notificationEmail", "ops@example.com"),
                        automationDsl()),
                blueprint("bpm-basic-v1", "bpm", "BPM Portal", "Workflow-oriented starter with managed order review.",
                        List.of("bpm", "commerce"),
                        List.of(
                                question("brandName", "What process or business name should be used?", true),
                                question("flowOwnerRole", "What role should approve workflow transitions?", true)
                        ),
                        Map.of("flowOwnerRole", "ROLE_ADMIN"),
                        bpmDsl()),
                blueprint("mixed-basic-v1", "mixed", "Mixed Business App", "Website, ecommerce, CRM, ERP reporting, and BPM-assisted starter app.",
                        List.of("website", "shop", "crm", "finance", "inventory", "notification", "bpm"),
                        List.of(
                                question("brandName", "What brand or business name should be used?", true),
                                question("homePageTitle", "What is the homepage title?", true),
                                question("starterProductName", "What starter product should be created?", true),
                                question("starterProductSku", "What SKU should the starter product use?", true),
                                question("warehouseKey", "What warehouse key should inventory use?", true)
                        ),
                        Map.of(
                                "starterProductSku", "STARTER-001",
                                "leadEntityKey", "crm-lead",
                                "warehouseKey", "main-warehouse",
                                "paymentProvider", "zarinpal-default"
                        ),
                        mixedDsl())
        );
    }

    private AppBlueprint blueprint(String key,
                                   String appType,
                                   String title,
                                   String description,
                                   List<String> capabilities,
                                   List<BlueprintQuestionDefinition> questions,
                                   Map<String, Object> defaultAnswers,
                                   PlatformAppDslDefinition dsl) {
        AppBlueprint blueprint = new AppBlueprint();
        blueprint.setBlueprintKey(key);
        blueprint.setAppType(appType);
        blueprint.setVersion(1);
        blueprint.setTitle(title);
        blueprint.setDescription(description);
        blueprint.setActive(true);
        blueprint.setCapabilities(new ArrayList<>(capabilities));
        blueprint.setRequiredQuestions(new ArrayList<>(questions));
        blueprint.setDefaultAnswers(new LinkedHashMap<>(defaultAnswers));
        blueprint.setBaseDsl(dsl);
        blueprint.setCreatedAt(Instant.now());
        blueprint.setUpdatedAt(Instant.now());
        return blueprint;
    }

    private BlueprintQuestionDefinition question(String key, String prompt, boolean required) {
        return new BlueprintQuestionDefinition(key, prompt, required);
    }

    private PlatformAppDslDefinition personalSiteDsl() {
        PlatformAppDslDefinition dsl = baseAppDsl(PlatformAppType.PERSONAL_SITE, "personal-site-basic");
        dsl.setEntities(List.of(
                contentEntity("landing-page", "landing-home"),
                contentEntity("landing-page", "page-about")
        ));
        dsl.setRoutes(List.of(
                route("home-route", "/", "content-service", "landing-page", "landing-home", "LANDING"),
                route("about-route", "/about", "content-service", "landing-page", "page-about", "PAGE")
        ));
        dsl.getDelivery().setPublicApis(List.of("/public/storefront/render?path=/", "/public/storefront/render?path=/about"));
        dsl.getDelivery().setBotApis(botApis());
        return dsl;
    }

    private PlatformAppDslDefinition companySiteDsl() {
        PlatformAppDslDefinition dsl = baseAppDsl(PlatformAppType.COMPANY_SITE, "company-site-basic");
        dsl.setEntities(List.of(
                contentEntity("landing-page", "landing-home"),
                contentEntity("landing-page", "page-about"),
                contentEntity("landing-page", "page-contact")
        ));
        dsl.setRoutes(List.of(
                route("home-route", "/", "content-service", "landing-page", "landing-home", "LANDING"),
                route("about-route", "/about", "content-service", "landing-page", "page-about", "PAGE"),
                route("contact-route", "/contact", "content-service", "landing-page", "page-contact", "PAGE")
        ));
        dsl.getDelivery().setPublicApis(List.of("/public/storefront/render?path=/", "/public/storefront/render?path=/about", "/public/storefront/render?path=/contact"));
        dsl.getDelivery().setBotApis(botApis());
        return dsl;
    }

    private PlatformAppDslDefinition blogDsl() {
        PlatformAppDslDefinition dsl = baseAppDsl(PlatformAppType.BLOG, "blog-basic");
        dsl.setEntities(List.of(
                contentEntity("landing-page", "landing-home"),
                contentEntity("blog-page", "blog-index")
        ));
        dsl.setRoutes(List.of(
                route("home-route", "/", "content-service", "landing-page", "landing-home", "LANDING"),
                route("blog-route", "/blog", "content-service", "blog-page", "blog-index", "BLOG")
        ));
        dsl.getDelivery().setPublicApis(List.of("/public/storefront/render?path=/", "/public/storefront/render?path=/blog"));
        dsl.getDelivery().setBotApis(botApis());
        return dsl;
    }

    private PlatformAppDslDefinition ecommerceDsl() {
        PlatformAppDslDefinition dsl = baseAppDsl(PlatformAppType.ECOMMERCE, "ecommerce-crm");
        dsl.setEntities(List.of(
                contentEntity("landing-page", "landing-home"),
                contentEntity("blog-page", "blog-index"),
                catalogEntity("catalog-product", "starter-product"),
                crmEntity("crm-contact", "starter-contact"),
                crmEntity("crm-lead", "starter-lead"),
                commerceEntity("sales-order", "starter-order"),
                commerceEntity("sales-invoice", "starter-invoice"),
                financeEntity("finance-transaction", "starter-transaction"),
                checkoutEntity("checkout-session", "starter-checkout"),
                paymentSessionEntity("payment-session", "starter-payment-session"),
                notificationTemplateEntity("notification-template", "order-created-notification"),
                reportEntity("dynamic-report", "shop-order-report")
        ));
        dsl.setRoutes(List.of(
                route("home-route", "/", "content-service", "landing-page", "landing-home", "LANDING"),
                route("blog-route", "/blog", "content-service", "blog-page", "blog-index", "BLOG"),
                route("product-route", "/products/starter-product", "catalog-service", "catalog-product", "starter-product", "PRODUCT")
        ));
        dsl.getDelivery().setPublicApis(List.of(
                "/public/storefront/render?path=/",
                "/public/storefront/render?path=/blog",
                "/public/storefront/render?path=/products/starter-product"
        ));
        dsl.getDelivery().setBotApis(List.of(
                "/endpoint/ai-orchestrator/drafts",
                "/endpoint/ai-orchestrator/sessions",
                "/endpoint/ai-orchestrator/drafts/{draftId}/provision",
                "/api/catalog-service/**",
                "/api/crm-service/**"
        ));
        dsl.setManualActions(new ArrayList<>(List.of(
                "If user chooses subdomain, bind storefront route ownership to the selected subdomain.",
                "Ensure payment-service has the zarinpal method enabled and configured before publish.",
                "Create a customer-facing Telegram bot after storefront publish to handle buyer interactions."
        )));
        return dsl;
    }

    private PlatformAppDslDefinition crmDsl() {
        PlatformAppDslDefinition dsl = baseAppDsl(PlatformAppType.CRM, "crm-basic");
        dsl.setEntities(List.of(
                crmEntity("crm-lead", "starter-lead"),
                crmEntity("crm-contact", "starter-contact")
        ));
        dsl.getDelivery().setPublicApis(List.of("/api/crm-service/**"));
        dsl.getDelivery().setBotApis(botApis());
        return dsl;
    }

    private PlatformAppDslDefinition invoiceDsl() {
        PlatformAppDslDefinition dsl = baseAppDsl(PlatformAppType.INVOICE_MANAGEMENT, "invoice-management");
        dsl.setEntities(List.of(
                commerceEntity("sales-invoice", "starter-invoice"),
                financeEntity("finance-transaction", "starter-transaction"),
                notificationTemplateEntity("notification-template", "invoice-issued-notification"),
                reportEntity("dynamic-report", "invoice-report")
        ));
        dsl.getDelivery().setPublicApis(List.of("/api/commerce-service/**", "/api/finance-service/**"));
        dsl.getDelivery().setBotApis(botApis());
        return dsl;
    }

    private PlatformAppDslDefinition erpDsl() {
        PlatformAppDslDefinition dsl = baseAppDsl(PlatformAppType.ERP, "erp-basic");
        dsl.setEntities(List.of(
                catalogEntity("catalog-product", "starter-product"),
                inventoryEntity("stock-item", "starter-stock"),
                commerceEntity("sales-order", "starter-order"),
                commerceEntity("sales-invoice", "starter-invoice"),
                financeEntity("finance-transaction", "starter-transaction"),
                reportEntity("dynamic-report", "erp-summary-report"),
                workOrderEntity("work-order", "starter-work-order")
        ));
        dsl.setFlows(List.of(orderReviewFlow("erp-order-review")));
        dsl.getDelivery().setPublicApis(List.of("/api/commerce-service/**", "/api/inventory-service/**", "/endpoint/bpm/flows"));
        dsl.getDelivery().setBotApis(botApisWithBpm());
        return dsl;
    }

    private PlatformAppDslDefinition automationDsl() {
        PlatformAppDslDefinition dsl = baseAppDsl(PlatformAppType.AUTOMATION, "automation-basic");
        dsl.setEntities(List.of(
                crmEntity("crm-lead", "automation-lead"),
                notificationTemplateEntity("notification-template", "lead-automation-notification"),
                reportEntity("dynamic-report", "automation-report")
        ));
        dsl.setFlows(List.of(orderReviewFlow("automation-review-flow")));
        dsl.getDelivery().setPublicApis(List.of("/api/crm-service/**", "/endpoint/bpm/flows"));
        dsl.getDelivery().setBotApis(botApisWithBpm());
        return dsl;
    }

    private PlatformAppDslDefinition bpmDsl() {
        PlatformAppDslDefinition dsl = baseAppDsl(PlatformAppType.BPM, "bpm-basic");
        dsl.setEntities(List.of(commerceEntity("sales-order", "starter-order")));
        dsl.setFlows(List.of(orderReviewFlow("managed-order-review")));
        dsl.getDelivery().setPublicApis(List.of("/endpoint/bpm/flows", "/endpoint/bpm/managed-objects"));
        dsl.getDelivery().setBotApis(botApisWithBpm());
        return dsl;
    }

    private PlatformAppDslDefinition mixedDsl() {
        PlatformAppDslDefinition dsl = baseAppDsl(PlatformAppType.MIXED_BUSINESS_APP, "mixed-basic");
        dsl.setEntities(List.of(
                contentEntity("landing-page", "landing-home"),
                contentEntity("blog-page", "blog-index"),
                catalogEntity("catalog-product", "starter-product"),
                crmEntity("crm-contact", "starter-contact"),
                crmEntity("crm-lead", "starter-lead"),
                commerceEntity("sales-order", "starter-order"),
                financeEntity("finance-transaction", "starter-transaction"),
                inventoryEntity("stock-item", "starter-stock"),
                notificationTemplateEntity("notification-template", "mixed-order-notification"),
                reportEntity("dynamic-report", "mixed-summary-report")
        ));
        dsl.setRoutes(List.of(
                route("home-route", "/", "content-service", "landing-page", "landing-home", "LANDING"),
                route("blog-route", "/blog", "content-service", "blog-page", "blog-index", "BLOG"),
                route("product-route", "/products/starter-product", "catalog-service", "catalog-product", "starter-product", "PRODUCT")
        ));
        dsl.setFlows(List.of(orderReviewFlow("mixed-order-review")));
        dsl.getDelivery().setPublicApis(List.of("/public/storefront/render?path=/", "/api/crm-service/**", "/endpoint/bpm/flows"));
        dsl.getDelivery().setBotApis(botApisWithBpm());
        dsl.setManualActions(new ArrayList<>(List.of(
                "Review the final route map and delivery channels before publish.",
                "Decide whether public deployment should use a platform subdomain or a connected custom domain."
        )));
        return dsl;
    }

    private PlatformAppDslDefinition baseAppDsl(PlatformAppType type, String appKey) {
        PlatformAppDslDefinition dsl = new PlatformAppDslDefinition();
        dsl.getApp().setAppKey(appKey);
        dsl.getApp().setType(type);
        dsl.setManualActions(new ArrayList<>());
        dsl.setFlows(new ArrayList<>());
        dsl.setRoutes(new ArrayList<>());
        dsl.setEntities(new ArrayList<>());
        dsl.setDelivery(new DeliveryBlueprint());
        return dsl;
    }

    private EntityBlueprint contentEntity(String templateKey, String recordKey) {
        EntityBlueprint entity = baseEntity("content-service", templateKey, templateKey, recordKey);
        entity.setCreateRecord(true);
        return entity;
    }

    private EntityBlueprint catalogEntity(String templateKey, String recordKey) {
        EntityBlueprint entity = baseEntity("catalog-service", templateKey, templateKey, recordKey);
        entity.setCreateRecord(true);
        return entity;
    }

    private EntityBlueprint crmEntity(String templateKey, String recordKey) {
        EntityBlueprint entity = baseEntity("crm-service", templateKey, templateKey, recordKey);
        entity.setCreateRecord(true);
        return entity;
    }

    private EntityBlueprint commerceEntity(String templateKey, String recordKey) {
        EntityBlueprint entity = baseEntity("commerce-service", templateKey, templateKey, recordKey);
        entity.setCreateRecord(true);
        return entity;
    }

    private EntityBlueprint financeEntity(String templateKey, String recordKey) {
        EntityBlueprint entity = baseEntity("finance-service", templateKey, templateKey, recordKey);
        entity.setCreateRecord(true);
        return entity;
    }

    private EntityBlueprint inventoryEntity(String templateKey, String recordKey) {
        EntityBlueprint entity = baseEntity("inventory-service", templateKey, templateKey, recordKey);
        entity.setCreateRecord(true);
        return entity;
    }

    private EntityBlueprint workOrderEntity(String templateKey, String recordKey) {
        EntityBlueprint entity = baseEntity("inventory-service", templateKey, templateKey, recordKey);
        entity.setCreateRecord(true);
        return entity;
    }

    private EntityBlueprint reportEntity(String templateKey, String recordKey) {
        return baseEntity("report-service", templateKey, templateKey, recordKey);
    }

    private EntityBlueprint checkoutEntity(String templateKey, String recordKey) {
        return baseEntity("checkout-service", templateKey, templateKey, recordKey);
    }

    private EntityBlueprint paymentSessionEntity(String templateKey, String recordKey) {
        return baseEntity("payment-orchestrator-service", templateKey, templateKey, recordKey);
    }

    private EntityBlueprint notificationTemplateEntity(String templateKey, String recordKey) {
        return baseEntity("notification-service", templateKey, templateKey, recordKey);
    }

    private EntityBlueprint baseEntity(String serviceKey, String templateKey, String entityKey, String recordKey) {
        EntityBlueprint entity = new EntityBlueprint();
        entity.setServiceKey(serviceKey);
        entity.setTemplateKey(templateKey);
        entity.setEntityKey(entityKey);
        entity.setRecordKey(recordKey);
        entity.setCreateDefinition(true);
        entity.setCreateRecord(false);
        entity.setRecordData(new LinkedHashMap<>());
        return entity;
    }

    private RouteBlueprint route(String routeKey, String path, String serviceKey, String entityKey, String recordKey, String pageType) {
        RouteBlueprint route = new RouteBlueprint();
        route.setRouteKey(routeKey);
        route.setPath(path);
        route.setTargetServiceKey(serviceKey);
        route.setTargetEntityKey(entityKey);
        route.setTargetRecordKey(recordKey);
        route.setThemeRecordKey("theme-main");
        route.setPageType(pageType);
        return route;
    }

    private FlowBlueprint orderReviewFlow(String flowKey) {
        FlowBlueprint flowBlueprint = new FlowBlueprint();
        flowBlueprint.setFlowKey(flowKey);
        flowBlueprint.setFlowDefinition(Map.of(
                "flowKey", flowKey,
                "name", "Order Review",
                "startState", "draft-order",
                "active", true,
                "states", List.of(
                        Map.of(
                                "id", "draft-order",
                                "displayName", "Draft Order",
                                "terminal", false,
                                "entityService", "commerce-service",
                                "entityKey", "sales-order",
                                "rendererService", "commerce-service",
                                "rendererKey", "sales-order",
                                "submitMode", "DYNAMIC",
                                "candidateGroups", List.of("ROLE_USER")
                        ),
                        Map.of(
                                "id", "approved-order",
                                "displayName", "Approved Order",
                                "terminal", true
                        )
                ),
                "transitions", List.of(
                        Map.of(
                                "id", "approve",
                                "fromState", "draft-order",
                                "toState", "approved-order",
                                "label", "Approve",
                                "allowedRoles", List.of("ROLE_ADMIN")
                        )
                )
        ));
        return flowBlueprint;
    }

    private List<String> botApis() {
        return List.of(
                "/endpoint/ai-orchestrator/drafts",
                "/endpoint/ai-orchestrator/sessions",
                "/endpoint/ai-orchestrator/drafts/{draftId}/provision"
        );
    }

    private List<String> botApisWithBpm() {
        return List.of(
                "/endpoint/ai-orchestrator/drafts",
                "/endpoint/ai-orchestrator/sessions",
                "/endpoint/ai-orchestrator/drafts/{draftId}/provision",
                "/endpoint/bpm/flows",
                "/endpoint/bpm/managed-objects"
        );
    }
}
