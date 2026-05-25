package com.cyancoder.aiorchestrator.service.impl;

import com.cyancoder.aiorchestrator.api.dto.CreateDraftRequest;
import com.cyancoder.aiorchestrator.repo.ClientAppDraftRepository;
import com.cyancoder.aiorchestrator.service.AppDraftService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BlueprintDraftBootstrapSeeder implements ApplicationRunner {
    private final ClientAppDraftRepository draftRepository;
    private final AppDraftService appDraftService;

    public BlueprintDraftBootstrapSeeder(ClientAppDraftRepository draftRepository,
                                         AppDraftService appDraftService) {
        this.draftRepository = draftRepository;
        this.appDraftService = appDraftService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (draftRepository.count() > 0) {
            return;
        }

        seed("personal-site", "tenant-demo", "site-personal", "client-demo", "Personal Site Demo", Map.of(
                "brandName", "Farid Profile",
                "homePageTitle", "Engineer, Builder, Operator",
                "aboutPageTitle", "About Me"
        ));
        seed("company-site", "tenant-demo", "site-company", "client-demo", "Company Site Demo", Map.of(
                "brandName", "Cyan Business",
                "homePageTitle", "Structured business apps without platform sprawl",
                "aboutPageTitle", "About Cyan"
        ));
        seed("e-commerce", "tenant-demo", "site-shop", "client-demo", "E-Commerce Demo", Map.of(
                "brandName", "Acme Store",
                "homePageTitle", "Acme Store",
                "starterProductName", "Acme Smartwatch X1",
                "starterProductSku", "WATCH-X1",
                "subdomainPrefix", "acme-store",
                "paymentProvider", "zarinpal-default"
        ));
        seed("crm", "tenant-demo", "site-crm", "client-demo", "CRM Demo", Map.of(
                "brandName", "Acme CRM",
                "leadEntityKey", "crm-lead"
        ));
        seed("erp", "tenant-demo", "site-erp", "client-demo", "ERP Demo", Map.of(
                "brandName", "Acme ERP",
                "warehouseKey", "main-warehouse",
                "defaultAccountKey", "main-account"
        ));
        seed("invoice-management", "tenant-demo", "site-invoice", "client-demo", "Invoice Demo", Map.of(
                "brandName", "Acme Billing",
                "defaultAccountKey", "main-account"
        ));
        seed("automation", "tenant-demo", "site-automation", "client-demo", "Automation Demo", Map.of(
                "brandName", "Acme Automation",
                "notificationEmail", "ops@acme.example"
        ));
        seed("bpm", "tenant-demo", "site-bpm", "client-demo", "BPM Demo", Map.of(
                "brandName", "Acme Workflow",
                "flowOwnerRole", "ROLE_ADMIN"
        ));
        seed("mixed", "tenant-demo", "site-mixed", "client-demo", "Mixed Demo", Map.of(
                "brandName", "Acme Platform",
                "homePageTitle", "Acme Platform",
                "starterProductName", "Acme Bundle",
                "starterProductSku", "ACME-BUNDLE-001",
                "warehouseKey", "main-warehouse"
        ));
    }

    private void seed(String appType,
                      String tenantKey,
                      String siteKey,
                      String clientKey,
                      String title,
                      Map<String, Object> answers) {
        appDraftService.createDraft(new CreateDraftRequest(
                appType,
                null,
                tenantKey,
                siteKey,
                clientKey,
                title,
                "Seeded demo draft for " + appType,
                answers
        ), "bootstrap-seed");
    }
}
