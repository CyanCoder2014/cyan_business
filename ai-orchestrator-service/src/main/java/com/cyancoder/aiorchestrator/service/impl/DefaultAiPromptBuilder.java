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

                Rules:
                1. Always use structured platform entities and routes.
                2. Prefer existing service templates from metadata below.
                3. Use content-service for pages/blog, catalog-service for products/services, crm-service for CRM, commerce-service for orders/invoices, finance-service for transactions, storefront-service for public routes, bpm-service for workflows.
                4. If domain purchase or DNS setup is required and no domain service exists, put it in manualActions.
                5. When the prompt suggests approvals or lifecycle review, create a BPM flow in flows[].
                6. When the prompt asks for BPM forms or form maker output, use bpm-service templates such as screening-intake-form and screening-review-form before inventing new structures.
                7. BPM automation states may use RUN_AUTOMATION_BLOCK with flowKey, variables, async, resultMappings, and store*At fields.
                8. delivery.publicApis must list useful public APIs for web/UI; delivery.botApis must list useful APIs for bot/telegram integration.
                9. Keep generated records minimal and valid against expected templates.
                10. Output JSON only.

                TenantKey: %s
                SiteKey: %s

                Platform metadata:
                %s

                Retrieved context:
                %s

                User prompt:
                %s
                """.formatted(tenantKey, siteKey, platformMetadata, contextSection, userPrompt);
    }
}
