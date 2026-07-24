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
                - resources[]
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
                    "capabilities": ["bpm", "automation"],
                    "availableServiceKeys": ["bpm-service", "automation-orchestrator-service"]
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
                  "resources": [
                    {
                      "resourceType": "PROCESSOR_DEFINITION",
                      "serviceKey": "processor-service",
                      "resourceKey": "request-normalizer",
                      "body": {
                        "processorKey": "request-normalizer",
                        "targetType": "BPM_FORM",
                        "validatorsJson": "[]",
                        "operatorsJson": "[]",
                        "description": "Normalize request fields",
                        "active": true
                      }
                    }
                  ],
                  "delivery": {
                    "publicApis": ["/public/storefront/render?path=/"],
                    "botApis": ["/endpoint/ai-orchestrator/drafts"]
                  },
                  "manualActions": []
                }

                Availability rules:
                1. _serviceAvailability.availableServiceKeys is authoritative for this request.
                2. Never create an entity, route, flow, resource, action, or dependency owned by an unavailable service.
                3. Copy the authoritative list into app.availableServiceKeys. Do not add services.
                4. If a requested feature needs an unavailable service, add a precise manualActions entry explaining which service must be enabled. Do not invent a replacement API.

                Platform composition rules:
                1. Always use structured platform entities and routes.
                2. Prefer existing service templates from metadata below.
                3. Use a service only when it appears in the authoritative available list.
                4. Use content-service for pages/blog, catalog-service for products/services, crm-service for CRM, commerce-service for orders/invoices, finance-service for transactions, storefront-service for public routes, bpm-service for workflows.
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

                BPM, form, entity, and processor rules:
                16. entityKey identifies the persisted structured definition and record schema in its owning service.
                17. formKey identifies the BPM state's UI form/renderer. Prefer rendererService+rendererKey; formKey may match entityKey for a simple BPM form.
                18. processorKey is optional. Use it only when reusable pre-submit validation, normalization, enrichment, or operations are required and processor-service is available.
                19. Create referenced entity definitions and processor resources before creating the BPM flow.
                20. BPM submission order is processor first, then target entity strict validation, then save. A processor failure must stop persistence.
                21. submitMode DYNAMIC requires entityService and entityKey and writes through dynamic entity APIs. submitMode STATIC requires submitUrl and must not pretend an entity definition will be saved.
                22. For nested object/list forms, represent nested fields with object/list types and itemValidations. Keep strict schemas: do not send undeclared extra fields.
                23. A form flow normally needs an entity definition and BPM flow. formKey is required only for a rendered state; processorKey is optional.

                Automation, scheduling, rules, and durability rules:
                24. BPM is the stateful business control plane and may invoke automation through RUN_AUTOMATION_BLOCK. Avoid cyclic BPM-to-automation-to-the-same-BPM designs.
                25. Use AUTOMATION_FLOW resources only when automation-orchestrator-service is available.
                26. For rule-engine behavior use JDM_DECISION/Zen-compatible decision definitions inside automation when supported; keep deterministic business decisions separate from form schema validation.
                27. Six-field cron is second minute hour day-of-month month day-of-week, for example 0 0 8 * * *. Always include an explicit IANA timezone.
                28. For ordinary small scheduled API work, use SCHEDULE_TRIGGER plus normal automation nodes.
                29. For large or important ETL, retries, skip/quarantine, chunk checkpoints, or crash recovery, use a BATCH_DEFINITION resource plus a VARIABLES automation flow containing SCHEDULE_TRIGGER -> RUN_BATCH_JOB -> END, but only if batch-worker-service is available.
                30. A batch runKey must be stable for one schedule occurrence, normally {{scheduledAt}}. Destination writes must use receiver-enforced idempotency or upsert semantics.
                31. Do not claim exactly-once remote HTTP effects unless the destination enforces Idempotency-Key.
                32. Do not place secrets in definitions, automation variables, or job parameters. Reference credential records or environment-variable names.
                33. Do not generate direct MySQL/PostgreSQL/MongoDB/ClickHouse access unless a supported connector is present in metadata. Otherwise use an API or manualActions.
                34. Reactive/Mono execution is not durable scheduling, leasing, checkpointing, or transaction recovery.

                Resource rules:
                35. resources[].resourceType may only be PROCESSOR_DEFINITION, AUTOMATION_FLOW, or BATCH_DEFINITION.
                36. PROCESSOR_DEFINITION must use processor-service; validatorsJson and operatorsJson must be valid JSON encoded as strings.
                37. AUTOMATION_FLOW must use automation-orchestrator-service and its body must be a complete active flow definition with stable flowKey/version, runtimeMode, nodes, and edges.
                38. BATCH_DEFINITION must use batch-worker-service and its body must contain definitionKey, title, active, and a bounded paginated API-to-API spec.
                39. SSO services establish identity/access boundaries; do not model business entities inside SSO.
                40. notification-service is for delivery side effects, media-service for assets, report-service for reporting definitions/read models, and processor-service does not own business persistence.

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
