package com.cyancoder.aiorchestrator.service.impl;

import com.cyancoder.aiorchestrator.api.dto.CreateDraftRequest;
import com.cyancoder.aiorchestrator.api.dto.UpdateDraftRequest;
import com.cyancoder.aiorchestrator.domain.AppBlueprint;
import com.cyancoder.aiorchestrator.domain.BlueprintQuestionDefinition;
import com.cyancoder.aiorchestrator.domain.ClientAppDraft;
import com.cyancoder.aiorchestrator.domain.DraftStatus;
import com.cyancoder.aiorchestrator.domain.EntityBlueprint;
import com.cyancoder.aiorchestrator.domain.PlatformAppDslDefinition;
import com.cyancoder.aiorchestrator.repo.ClientAppDraftRepository;
import com.cyancoder.aiorchestrator.service.AppDraftService;
import com.cyancoder.aiorchestrator.service.BlueprintCatalogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class MongoAppDraftService implements AppDraftService {
    private final ClientAppDraftRepository repository;
    private final BlueprintCatalogService blueprintCatalogService;
    private final ObjectMapper objectMapper;

    public MongoAppDraftService(ClientAppDraftRepository repository,
                                BlueprintCatalogService blueprintCatalogService,
                                ObjectMapper objectMapper) {
        this.repository = repository;
        this.blueprintCatalogService = blueprintCatalogService;
        this.objectMapper = objectMapper;
    }

    @Override
    public ClientAppDraft createDraft(CreateDraftRequest request, String createdBy) {
        AppBlueprint blueprint = request.blueprintKey() != null && !request.blueprintKey().isBlank()
                ? blueprintCatalogService.getActiveByBlueprintKey(request.blueprintKey())
                : blueprintCatalogService.resolveActiveByType(resolveAppType(request.appType(), request.prompt()));
        Map<String, Object> answers = new LinkedHashMap<>(blueprint.getDefaultAnswers());
        if (request.answers() != null) {
            answers.putAll(request.answers());
        }
        ClientAppDraft draft = new ClientAppDraft();
        draft.setDraftId("draft-" + UUID.randomUUID());
        draft.setTenantKey(defaultScope(request.tenantKey(), "tenant-" + slug(blueprint.getAppType())));
        draft.setSiteKey(defaultScope(request.siteKey(), "site-" + slug(blueprint.getAppType())));
        draft.setClientKey(request.clientKey());
        draft.setBlueprintKey(blueprint.getBlueprintKey());
        draft.setBlueprintVersion(blueprint.getVersion());
        draft.setAppType(blueprint.getAppType());
        draft.setTitle(firstNonBlank(request.title(), blueprint.getTitle()));
        draft.setLatestIntent(firstNonBlank(request.prompt(), blueprint.getDescription()));
        draft.setAnswers(answers);
        draft.setRevision(1);
        draft.setCreatedAt(Instant.now());
        draft.setUpdatedAt(Instant.now());
        draft.setCreatedBy(createdBy);
        draft.setUpdatedBy(createdBy);
        applyResolvedDsl(draft, blueprint);
        return repository.save(draft);
    }

    @Override
    public List<ClientAppDraft> listDrafts(String tenantKey, String siteKey, String clientKey) {
        if (clientKey != null && !clientKey.isBlank()) {
            return repository.findByClientKeyOrderByUpdatedAtDesc(clientKey);
        }
        if (tenantKey != null && !tenantKey.isBlank() && siteKey != null && !siteKey.isBlank()) {
            return repository.findByTenantKeyAndSiteKeyOrderByUpdatedAtDesc(tenantKey, siteKey);
        }
        if (tenantKey != null && !tenantKey.isBlank()) {
            return repository.findByTenantKeyOrderByUpdatedAtDesc(tenantKey);
        }
        return repository.findAll();
    }

    @Override
    public ClientAppDraft getDraft(String draftId) {
        return repository.findByDraftId(draftId).orElseThrow();
    }

    @Override
    public ClientAppDraft updateDraft(String draftId, UpdateDraftRequest request, String updatedBy) {
        ClientAppDraft draft = getDraft(draftId);
        AppBlueprint blueprint = blueprintCatalogService.getActiveByBlueprintKey(draft.getBlueprintKey());
        if (request.title() != null && !request.title().isBlank()) {
            draft.setTitle(request.title());
        }
        if (request.prompt() != null && !request.prompt().isBlank()) {
            draft.setLatestIntent(request.prompt());
        }
        if (request.answersPatch() != null) {
            draft.getAnswers().putAll(request.answersPatch());
        }
        draft.setRevision((draft.getRevision() == null ? 0 : draft.getRevision()) + 1);
        draft.setUpdatedAt(Instant.now());
        draft.setUpdatedBy(updatedBy);
        applyResolvedDsl(draft, blueprint);
        return repository.save(draft);
    }

    @Override
    public Optional<ClientAppDraft> resolveKnownAppDraft(String appType, String tenantKey, String siteKey, String clientKey, String prompt) {
        String resolvedAppType = resolveAppType(appType, prompt);
        if (resolvedAppType == null) {
            return Optional.empty();
        }
        if (tenantKey != null && !tenantKey.isBlank() && siteKey != null && !siteKey.isBlank()) {
            Optional<ClientAppDraft> existing = clientKey != null && !clientKey.isBlank()
                    ? repository.findFirstByTenantKeyAndSiteKeyAndClientKeyAndAppTypeOrderByUpdatedAtDesc(tenantKey, siteKey, clientKey, resolvedAppType)
                    : repository.findFirstByTenantKeyAndSiteKeyAndAppTypeOrderByUpdatedAtDesc(tenantKey, siteKey, resolvedAppType);
            if (existing.isPresent()) {
                return existing;
            }
        }
        CreateDraftRequest request = new CreateDraftRequest(resolvedAppType, null, tenantKey, siteKey, clientKey, null, prompt, Map.of());
        return Optional.of(createDraft(request, "system-generate"));
    }

    private void applyResolvedDsl(ClientAppDraft draft, AppBlueprint blueprint) {
        PlatformAppDslDefinition dsl = objectMapper.convertValue(blueprint.getBaseDsl(), PlatformAppDslDefinition.class);
        dsl.getApp().setTitle(firstNonBlank(draft.getTitle(), blueprint.getTitle()));
        dsl.getApp().setTenantKey(draft.getTenantKey());
        dsl.getApp().setSiteKey(draft.getSiteKey());
        dsl.getApp().setCapabilities(new ArrayList<>(blueprint.getCapabilities()));
        dsl.setManualActions(new ArrayList<>());
        enrichEntities(dsl, draft.getAnswers(), blueprint.getAppType());
        draft.setResolvedDsl(dsl);
        List<String> pendingQuestions = determinePendingQuestions(blueprint, draft.getAnswers());
        draft.setPendingQuestions(pendingQuestions);
        draft.setManualActions(new ArrayList<>(dsl.getManualActions()));
        draft.setStatus(pendingQuestions.isEmpty() ? DraftStatus.READY : DraftStatus.DRAFT);
    }

    private void enrichEntities(PlatformAppDslDefinition dsl, Map<String, Object> answers, String appType) {
        for (EntityBlueprint entity : dsl.getEntities()) {
            Map<String, Object> recordData = new LinkedHashMap<>(entity.getRecordData());
            if ("content-service".equals(entity.getServiceKey()) && "landing-page".equals(entity.getTemplateKey())) {
                recordData.putIfAbsent("title", firstNonBlank(stringValue(answers.get("homePageTitle")), stringValue(answers.get("brandName")), "Home"));
                recordData.putIfAbsent("slug", "landing-home".equals(entity.getRecordKey()) ? "home" : entity.getRecordKey());
                recordData.putIfAbsent("heroTitle", firstNonBlank(stringValue(answers.get("homePageTitle")), stringValue(answers.get("brandName")), "Welcome"));
                recordData.putIfAbsent("heroSubtitle", "Generated from deterministic orchestrator blueprint.");
                recordData.putIfAbsent("publicationStatus", "DRAFT");
                recordData.putIfAbsent("sections", List.of());
            } else if ("content-service".equals(entity.getServiceKey()) && "blog-page".equals(entity.getTemplateKey())) {
                recordData.putIfAbsent("slug", firstNonBlank(stringValue(answers.get("blogSlug")), "blog"));
                recordData.putIfAbsent("title", "Blog");
                recordData.putIfAbsent("summary", "Blog index for " + firstNonBlank(stringValue(answers.get("brandName")), "the site"));
                recordData.putIfAbsent("body", "Generated blog starter page.");
                recordData.putIfAbsent("author", firstNonBlank(stringValue(answers.get("brandName")), "Editorial Team"));
                recordData.putIfAbsent("publicationStatus", "DRAFT");
                recordData.putIfAbsent("tags", List.of("starter"));
            } else if ("catalog-service".equals(entity.getServiceKey())) {
                recordData.putIfAbsent("name", firstNonBlank(stringValue(answers.get("starterProductName")), "Starter Product"));
                recordData.putIfAbsent("sku", firstNonBlank(stringValue(answers.get("starterProductSku")), "STARTER-001"));
                recordData.putIfAbsent("categoryKey", "platform");
                recordData.putIfAbsent("unit", "pcs");
                recordData.putIfAbsent("defaultPrice", 0);
                recordData.putIfAbsent("currency", "IRR");
                recordData.putIfAbsent("active", true);
                recordData.putIfAbsent("slug", slug(firstNonBlank(stringValue(answers.get("starterProductName")), "starter-product")));
                recordData.putIfAbsent("details", Map.of("brand", firstNonBlank(stringValue(answers.get("brandName")), "Brand"), "shortDescription", "Starter product generated from blueprint."));
            } else if ("crm-service".equals(entity.getServiceKey()) && "crm-contact".equals(entity.getTemplateKey())) {
                recordData.putIfAbsent("recordType", "CONTACT");
                recordData.putIfAbsent("fullName", firstNonBlank(stringValue(answers.get("contactFullName")), "Primary Customer"));
                recordData.putIfAbsent("companyName", firstNonBlank(stringValue(answers.get("brandName")), "Business"));
                recordData.putIfAbsent("email", firstNonBlank(stringValue(answers.get("contactEmail")), "customer@example.com"));
                recordData.putIfAbsent("mobile", firstNonBlank(stringValue(answers.get("contactMobile")), "09120000000"));
                recordData.putIfAbsent("status", "ACTIVE");
                recordData.putIfAbsent("source", "BLUEPRINT");
                recordData.putIfAbsent("notes", "Starter contact generated from blueprint.");
            } else if ("crm-service".equals(entity.getServiceKey()) && "crm-lead".equals(entity.getTemplateKey())) {
                recordData.putIfAbsent("recordType", "LEAD");
                recordData.putIfAbsent("fullName", firstNonBlank(stringValue(answers.get("leadFullName")), "Primary Lead"));
                recordData.putIfAbsent("companyName", firstNonBlank(stringValue(answers.get("brandName")), "Business"));
                recordData.putIfAbsent("email", firstNonBlank(stringValue(answers.get("leadEmail")), "lead@example.com"));
                recordData.putIfAbsent("mobile", firstNonBlank(stringValue(answers.get("leadMobile")), "09120000001"));
                recordData.putIfAbsent("status", "NEW");
                recordData.putIfAbsent("source", "WEBSITE");
                recordData.putIfAbsent("ownerUserId", "sales-01");
                recordData.putIfAbsent("notes", "Starter lead generated from blueprint.");
            } else if ("commerce-service".equals(entity.getServiceKey()) && "sales-order".equals(entity.getTemplateKey())) {
                recordData.putIfAbsent("documentType", "ORDER");
                recordData.putIfAbsent("customerKey", "starter-contact");
                recordData.putIfAbsent("currency", "IRR");
                recordData.putIfAbsent("documentStatus", "DRAFT");
                recordData.putIfAbsent("subtotal", 0);
                recordData.putIfAbsent("discountTotal", 0);
                recordData.putIfAbsent("taxTotal", 0);
                recordData.putIfAbsent("grandTotal", 0);
                recordData.putIfAbsent("items", List.of(Map.of(
                        "itemKey", "starter-product",
                        "name", firstNonBlank(stringValue(answers.get("starterProductName")), "Starter Product"),
                        "quantity", 1,
                        "unitPrice", 0,
                        "lineTotal", 0
                )));
            } else if ("commerce-service".equals(entity.getServiceKey()) && "sales-invoice".equals(entity.getTemplateKey())) {
                recordData.putIfAbsent("documentType", "INVOICE");
                recordData.putIfAbsent("customerKey", "starter-contact");
                recordData.putIfAbsent("currency", "IRR");
                recordData.putIfAbsent("documentStatus", "ISSUED");
                recordData.putIfAbsent("subtotal", 0);
                recordData.putIfAbsent("discountTotal", 0);
                recordData.putIfAbsent("taxTotal", 0);
                recordData.putIfAbsent("grandTotal", 0);
                recordData.putIfAbsent("items", List.of(Map.of(
                        "itemKey", "starter-product",
                        "quantity", 1,
                        "unitPrice", 0,
                        "lineTotal", 0
                )));
            } else if ("finance-service".equals(entity.getServiceKey()) && "finance-transaction".equals(entity.getTemplateKey())) {
                recordData.putIfAbsent("transactionType", "PAYMENT");
                recordData.putIfAbsent("referenceType", "INVOICE");
                recordData.putIfAbsent("referenceKey", "starter-invoice");
                recordData.putIfAbsent("accountKey", firstNonBlank(stringValue(answers.get("defaultAccountKey")), "main-account"));
                recordData.putIfAbsent("currency", "IRR");
                recordData.putIfAbsent("amount", 0);
                recordData.putIfAbsent("status", "PENDING");
                recordData.putIfAbsent("description", "Starter finance transaction generated from blueprint.");
            } else if ("inventory-service".equals(entity.getServiceKey()) && "stock-item".equals(entity.getTemplateKey())) {
                recordData.putIfAbsent("catalogItemKey", "starter-product");
                recordData.putIfAbsent("warehouseKey", firstNonBlank(stringValue(answers.get("warehouseKey")), "main-warehouse"));
                recordData.putIfAbsent("onHandQuantity", 0);
                recordData.putIfAbsent("reservedQuantity", 0);
                recordData.putIfAbsent("reorderPoint", 1);
                recordData.putIfAbsent("unit", "pcs");
            } else if ("inventory-service".equals(entity.getServiceKey()) && "work-order".equals(entity.getTemplateKey())) {
                recordData.putIfAbsent("workOrderCode", "WO-STARTER-001");
                recordData.putIfAbsent("catalogItemKey", "starter-product");
                recordData.putIfAbsent("plannedQuantity", 1);
                recordData.putIfAbsent("status", "PLANNED");
                recordData.putIfAbsent("operations", List.of(Map.of(
                        "name", "Assemble starter item",
                        "workCenter", "default-center",
                        "durationMinutes", 30
                )));
            }
            entity.setRecordData(recordData);
        }
        if ("shop".equals(appType) || "e-commerce".equals(appType) || "mixed".equals(appType) || "erp".equals(appType)) {
            dsl.getManualActions().add("Review pricing, payment providers, and notification templates before publish.");
        }
    }

    private List<String> determinePendingQuestions(AppBlueprint blueprint, Map<String, Object> answers) {
        List<String> pending = new ArrayList<>();
        for (BlueprintQuestionDefinition question : blueprint.getRequiredQuestions()) {
            if (!question.isRequired()) {
                continue;
            }
            Object value = answers.get(question.getKey());
            if (value == null || String.valueOf(value).isBlank()) {
                pending.add(question.getPrompt());
            }
        }
        return pending;
    }

    private String resolveAppType(String appType, String prompt) {
        if (appType != null && !appType.isBlank()) {
            return normalizeAppType(appType);
        }
        if (prompt == null) {
            return null;
        }
        String lower = prompt.toLowerCase(Locale.ROOT);
        if (lower.contains("personal site") || lower.contains("portfolio") || lower.contains("resume site")) return "personal-site";
        if (lower.contains("company") || lower.contains("corporate")) return "company-site";
        if (lower.contains("blog")) return "blog";
        if (lower.contains("erp")) return "erp";
        if (lower.contains("invoice")) return "invoice-management";
        if (lower.contains("automation")) return "automation";
        if (lower.contains("bpm")) return "bpm";
        if (lower.contains("shop") || lower.contains("ecommerce") || lower.contains("e-commerce")) return "e-commerce";
        if (lower.contains("crm") || lower.contains("lead") || lower.contains("contact")) return "crm";
        if (lower.contains("bpm") || lower.contains("mixed")) return "mixed";
        return null;
    }

    private String normalizeAppType(String appType) {
        String normalized = appType.trim().toLowerCase(Locale.ROOT);
        if ("company_site".equals(normalized) || "companysite".equals(normalized)) {
            return "company-site";
        }
        if ("personal_site".equals(normalized) || "personalsite".equals(normalized)) {
            return "personal-site";
        }
        if ("ecommerce".equals(normalized) || "shop".equals(normalized)) {
            return "e-commerce";
        }
        if ("invoice".equals(normalized) || "invoicemanagement".equals(normalized) || "invoice_management".equals(normalized)) {
            return "invoice-management";
        }
        return normalized;
    }

    private String defaultScope(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String slug(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
