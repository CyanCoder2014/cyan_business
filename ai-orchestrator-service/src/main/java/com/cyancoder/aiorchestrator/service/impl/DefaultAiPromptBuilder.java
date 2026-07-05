package com.cyancoder.aiorchestrator.service.impl;

import com.cyancoder.aiorchestrator.service.AiPromptBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DefaultAiPromptBuilder implements AiPromptBuilder {
    @Override
    public String buildPlatformPrompt(String userPrompt,
                                      Map<String, Object> platformMetadata,
                                      List<String> retrievedContext,
                                      String tenantKey,
                                      String siteKey) {
        String contextSection = retrievedContext.isEmpty()
                ? "No external context retrieved."
                : String.join("\n---\n", retrievedContext);
        return """
                Generate platform application DSL from the user request.

                Output must match PlatformAppDslDefinition JSON shape:
                - app
                - entities[]
                - routes[]
                - flows[]
                - delivery
                - manualActions[]

                Required JSON contract:
                {
                  "app": {
                    "appKey": "kebab-case-key",
                    "title": "Human title",
                    "type": "MIXED_BUSINESS_APP",
                    "tenantKey": "%s",
                    "siteKey": "%s",
                    "desiredDomain": null,
                    "capabilities": ["website", "bpm"]
                  },
                  "entities": [
                    {
                      "serviceKey": "bpm-service",
                      "templateKey": "screening-intake-form",
                      "entityKey": "request-intake",
                      "recordKey": "request-intake",
                      "createDefinition": true,
                      "createRecord": false,
                      "recordData": {}
                    }
                  ],
                  "routes": [],
                  "flows": [
                    {
                      "flowKey": "request-review",
                      "flowDefinition": {}
                    }
                  ],
                  "delivery": {
                    "publicApis": ["/public/storefront/render?path=/"],
                    "botApis": ["/endpoint/ai-orchestrator/drafts"]
                  },
                  "manualActions": []
                }

                Rules:
                1. Always use structured platform entities and routes.
                2. Prefer existing service templates from metadata below.
                3. Use content-service for pages/blog, catalog-service for products/services, crm-service for CRM, commerce-service for orders/invoices, finance-service for transactions, storefront-service for public routes, bpm-service for workflows.
                4. If domain purchase or DNS setup is required and no domain service exists, put it in manualActions.
                5. When the prompt suggests approvals or lifecycle review, create a BPM flow in flows[].
                6. When the prompt asks for BPM forms or form maker output, use bpm-service templates such as screening-intake-form and screening-review-form before inventing new structures.
                7. BPM automation states may use RUN_AUTOMATION_BLOCK with flowKey, variables, async, resultMappings, and store*At fields.
                8. delivery.publicApis and delivery.botApis must be arrays of URL/path strings only, never objects.
                9. Keep generated records minimal and valid against expected templates.
                10. Output JSON only.
                11. app.appKey is mandatory and must be kebab-case.
                12. app.type must be one of PERSONAL_SITE, COMPANY_SITE, WEBSITE, BLOG, SHOP, ECOMMERCE, CRM, ERP, INVOICE_MANAGEMENT, AUTOMATION, FORM_FLOW, BPM, BPM_PORTAL, MIXED_BUSINESS_APP.
                13. Entity objects must use serviceKey/templateKey/entityKey/recordKey; never use service, entityType, or templateName.
                14. Flow objects must use flowKey and flowDefinition.
                15. Do not include clientKey inside app; keep tenantKey and siteKey only.

                TenantKey: %s
                SiteKey: %s

                Platform metadata:
                %s

                Retrieved context:
                %s

                User prompt:
                %s
                """.formatted(tenantKey, siteKey, tenantKey, siteKey, platformMetadata, contextSection, userPrompt);
    }
}
