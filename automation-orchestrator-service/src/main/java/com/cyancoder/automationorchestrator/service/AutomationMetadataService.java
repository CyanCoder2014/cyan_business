package com.cyancoder.automationorchestrator.service;

import com.cyancoder.automationorchestrator.domain.AutomationNodeType;
import com.cyancoder.automationorchestrator.model.MetadataFieldDescriptor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AutomationMetadataService {
    private static final List<MetadataFieldDescriptor> COMMON = List.of(
            f("id", "string", true, "Stable node id, unique within the flow.", "node-1"),
            f("name", "string", true, "Display name shown on the canvas.", "Fetch customer record"),
            f("enabled", "boolean", false, "Disabled nodes are skipped at runtime without breaking the flow.", true),
            f("credentialRef", "string", false, "Reference to a stored credential; the raw secret is never stored on the node.", "cred-crm-api"),
            f("retryPolicy", "object", false, "maxAttempts, backoffMs, strategy for transient failures.", Map.of("maxAttempts", 3, "backoffMs", 1000, "strategy", "EXPONENTIAL")),
            f("timeoutPolicy", "object", false, "connectTimeoutMs, readTimeoutMs for outbound calls made by this node.", Map.of("connectTimeoutMs", 3000, "readTimeoutMs", 10000)),
            f("errorPolicy", "object", false, "continueOnFail, deadLetterOnFailure, fallbackNodeId for handling node failure.", Map.of("continueOnFail", false)),
            f("concurrencyPolicy", "object", false, "keyExpression, maxConcurrency to serialize or cap parallel runs sharing a key.", Map.of("maxConcurrency", 1)),
            f("config", "object", false, "Node-type-specific parameters; see the fields below.", Map.of())
    );

    public List<Map<String, Object>> nodes() {
        Map<AutomationNodeType, List<MetadataFieldDescriptor>> configs = new LinkedHashMap<>();
        configs.put(AutomationNodeType.WEBHOOK_TRIGGER, List.of());
        configs.put(AutomationNodeType.MANUAL_TRIGGER, List.of());
        configs.put(AutomationNodeType.SCHEDULE_TRIGGER, List.of(
                f("cron", "string", false, "Cron expression for the schedule. Alternative to intervalSeconds.", "0 9 * * 1"),
                f("intervalSeconds", "integer", false, "Fixed interval in seconds. Alternative to cron.", 3600),
                f("timezone", "string", false, "IANA timezone used to evaluate the schedule.", "UTC")
        ));
        configs.put(AutomationNodeType.ERROR_TRIGGER, List.of());
        configs.put(AutomationNodeType.WAIT, List.of(
                f("delaySeconds", "integer", false, "Pause the flow for this many seconds. Alternative to resumeAt.", 60),
                f("resumeAt", "string", false, "ISO-8601 timestamp to resume at. Alternative to delaySeconds.", "2026-01-01T09:00:00Z")
        ));
        configs.put(AutomationNodeType.WAIT_FOR_CALLBACK, List.of(
                f("callbackMappings", "object", false, "Map of variable name -> path into the incoming callback payload.", Map.of("riskScore", "score")),
                f("callbackStorePath", "string", false, "Variable path to store the full raw callback payload at.", "screeningCallback")
        ));
        configs.put(AutomationNodeType.CALL_API, callApiParams(false));
        configs.put(AutomationNodeType.HTTP_REQUEST, List.of(
                f("url", "string", false, "Direct URL for external calls. Alternative to serviceKey+path.", "https://example.test/verify"),
                f("serviceKey", "string", false, "Target internal service key. Alternative to url.", "processor-service"),
                f("path", "string", false, "Target internal path, used together with serviceKey.", "/internal/processor/forms/submit"),
                f("method", "string", false, "HTTP method. Defaults to GET.", "GET"),
                f("headers", "object", false, "Map of HTTP header name -> value.", Map.of("X-API-KEY", "secret")),
                f("body", "object|array|string", false, "Request body; supports {{variable}} templating.", Map.of("nationalCode", "{{payload.currentFormValues.nationalCode}}")),
                f("responsePath", "string", false, "Path into the response to extract before storing.", "data.result"),
                f("executeOnce", "boolean", false, "If true, run once for the whole batch instead of once per item in N8N_ITEMS mode.", false)
        ));
        configs.put(AutomationNodeType.PAGINATED_CALL_API, List.of(
                f("url", "string", false, "Direct URL for external calls. Alternative to serviceKey+path.", "https://example.test/records"),
                f("serviceKey", "string", false, "Target internal service key. Alternative to url.", "crm-service"),
                f("path", "string", false, "Target internal path, used together with serviceKey.", "/internal/records"),
                f("method", "string", false, "HTTP method. Defaults to GET.", "GET"),
                f("headers", "object", false, "Map of HTTP header name -> value.", Map.of()),
                f("body", "object|array|string", false, "Request body sent with every page request.", Map.of()),
                f("pageStart", "integer", false, "First page number to request.", 0),
                f("pageEnd", "integer", false, "Last page number to request. Alternative to pageCount.", 10),
                f("pageCount", "integer", false, "Total number of pages to request. Alternative to pageEnd.", 10),
                f("pageParamPath", "string", false, "Request field/query path that carries the page number.", "page"),
                f("size", "integer", false, "Page size requested per call.", 100),
                f("sizeParamPath", "string", false, "Request field/query path that carries the page size.", "size"),
                f("itemsPath", "string", false, "Path into each page's response holding the item array.", "data.items"),
                f("targetPath", "string", false, "Variable path to accumulate all items into.", "records"),
                f("pageResponsesPath", "string", false, "Optional variable path to also store every raw page response.", "rawPages"),
                f("stopOnEmpty", "boolean", false, "Stop paginating once a page returns no items.", true),
                f("responseMappings", "object", false, "Map of variable path -> response path, applied per page.", Map.of())
        ));
        configs.put(AutomationNodeType.RUN_BATCH_JOB, List.of(
                f("definitionKey", "string", true, "Batch job definition key to run.", "monthly-invoice-export"),
                f("runKey", "string", false, "Idempotency key for this batch run; auto-generated when omitted.", "{{executionId}}"),
                f("pollSeconds", "integer", false, "Interval to poll the batch run status while waiting.", 5),
                f("resultPath", "string", false, "Variable path to store the batch run's result at.", "batchResult")
        ));
        configs.put(AutomationNodeType.AI_OPERATION, List.of(
                f("operation", "string", true, "TRANSFORM_DATA, GENERATE_CONTENT, or GENERATE_DSL.", "TRANSFORM_DATA"),
                f("instructions", "string", true, "Precise natural-language instructions for the AI operation.", "Summarize the ticket into one sentence"),
                f("input", "string|object", false, "Input data or expression passed to the operation.", "{{payload.ticket}}"),
                f("outputSchema", "object", false, "Optional JSON schema the AI output must conform to.", Map.of()),
                f("locale", "string", false, "Locale for generated content.", "en"),
                f("providerProfileKey", "string", false, "AI provider profile to use; falls back to the tenant default.", "default"),
                f("assets", "array<string>", false, "Media asset keys made available to the operation.", List.of()),
                f("resultPath", "string", false, "Variable path to store the AI result at.", "aiResult")
        ));
        configs.put(AutomationNodeType.IF, conditionFields());
        configs.put(AutomationNodeType.SWITCH, List.of(
                f("field", "string", true, "Variable path evaluated to pick a branch.", "payload.status"),
                f("cases", "object", true, "Map of case value -> output port name; unmatched values use the default port.", Map.of("PAID", "paid", "FAILED", "failed"))
        ));
        configs.put(AutomationNodeType.MERGE, List.of());
        configs.put(AutomationNodeType.FOR_EACH, List.of(
                f("sourcePath", "string", true, "Variable path to an array to iterate over.", "records"),
                f("chunkSize", "integer", false, "Number of items processed per iteration batch.", 1),
                f("itemTemplate", "object", false, "Template applied to each item before downstream nodes see it.", Map.of()),
                f("targetPath", "string", false, "Variable path to write the per-item results back to.", "results")
        ));
        configs.put(AutomationNodeType.LOOP_OVER_ITEMS, List.of(
                f("batchSize", "integer", false, "Number of items sent down the loop port per iteration.", 1)
        ));
        configs.put(AutomationNodeType.SUBFLOW, List.of(
                f("flowKey", "string", true, "Active automation flow key to run as a sub-flow.", "send-welcome-email"),
                f("resultPath", "string", false, "Variable path to store the sub-flow's output at.", "subflowResult")
        ));
        configs.put(AutomationNodeType.EXECUTE_WORKFLOW, List.of(
                f("flowKey", "string", true, "Active automation flow key to execute.", "sync-inventory")
        ));
        configs.put(AutomationNodeType.JDM_DECISION, List.of(
                f("jdm", "object", false, "Inline JDM decision graph. Alternative to classpathResource/filePath.", Map.of()),
                f("classpathResource", "string", false, "Classpath location of a .jdm resource. Alternative to jdm/filePath.", "decisions/pricing.jdm"),
                f("filePath", "string", false, "Filesystem path of a .jdm resource. Alternative to jdm/classpathResource.", "/decisions/pricing.jdm"),
                f("inputPath", "string", false, "Variable path of the input handed to the decision. Alternative to inputTemplate.", "payload"),
                f("inputTemplate", "object", false, "Templated input handed to the decision. Alternative to inputPath.", Map.of()),
                f("outputPath", "string", false, "Variable path to store the decision output at.", "decisionResult"),
                f("trace", "boolean", false, "Include the decision evaluation trace in the result.", false),
                f("tracePath", "string", false, "Variable path to store the trace at, when trace is enabled.", "decisionTrace"),
                f("performancePath", "string", false, "Variable path to store evaluation timing at.", "decisionTiming")
        ));
        configs.put(AutomationNodeType.MAP_FIELDS, List.of(
                f("mappings", "object", true, "Map of target variable path -> source expression.", Map.of("customerName", "{{payload.name}}"))
        ));
        configs.put(AutomationNodeType.EDIT_FIELDS, List.of(
                f("assignments", "object", true, "Map of field path -> value to set on the current item.", Map.of("status", "REVIEWED")),
                f("keepOnlySet", "boolean", false, "If true, drop every field not listed in assignments.", false)
        ));
        configs.put(AutomationNodeType.JSON_TRANSFORM, List.of(
                f("sourcePath", "string", true, "Variable path to the JSON value to transform.", "payload"),
                f("template", "object", true, "Output template; supports {{variable}} references into the source.", Map.of()),
                f("targetPath", "string", false, "Variable path to store the transformed result at.", "transformed")
        ));
        configs.put(AutomationNodeType.FILTER, conditionFields());
        configs.put(AutomationNodeType.SPLIT_OUT, List.of(
                f("field", "string", true, "Variable path to an array field to split into separate items.", "lineItems"),
                f("targetField", "string", false, "Field name each split-out value is placed under. Defaults to the source field name.", "lineItem")
        ));
        configs.put(AutomationNodeType.AGGREGATE, List.of(
                f("field", "string", true, "Source field aggregated across items.", "amount"),
                f("targetField", "string", false, "Field name to store the aggregated value under.", "totalAmount"),
                f("groupByField", "string", false, "Field to group items by before aggregating.", "customerId"),
                f("groupKeyField", "string", false, "Field name to store the group key under in each group's result.", "customerId"),
                f("skipBlankKeys", "boolean", false, "Skip items whose group key is blank instead of grouping them together.", true),
                f("operation", "string", true, "SUM, AVG, MIN, MAX, or COUNT.", "SUM"),
                f("sortByField", "string", false, "Field to sort the aggregated groups by.", "totalAmount"),
                f("direction", "string", false, "ASC or DESC, used with sortByField.", "DESC"),
                f("aggregations", "array<object>", false, "Additional {field, operation, targetField} aggregations computed in the same pass.", List.of())
        ));
        configs.put(AutomationNodeType.SORT, List.of(
                f("field", "string", true, "Field to sort items by.", "createdAt"),
                f("direction", "string", false, "ASC or DESC. Defaults to ASC.", "DESC")
        ));
        configs.put(AutomationNodeType.LIMIT, List.of(
                f("maxItems", "integer", true, "Maximum number of items to keep.", 50),
                f("keep", "string", false, "FIRST or LAST. Defaults to FIRST.", "FIRST")
        ));
        configs.put(AutomationNodeType.FILE_METADATA, List.of(
                f("sourcePath", "string", true, "Variable path to a file reference to inspect.", "payload.attachment"),
                f("targetPath", "string", false, "Variable path to store the extracted metadata at.", "attachmentMeta")
        ));
        configs.put(AutomationNodeType.DEDUP_BY_KEY, List.of(
                f("sourcePath", "string", true, "Variable path to an array to deduplicate.", "records"),
                f("keyPath", "string", true, "Field path used as the dedup key for each item.", "email"),
                f("targetPath", "string", false, "Variable path to store the deduplicated array at. Defaults to sourcePath.", "records"),
                f("keep", "string", false, "FIRST or LAST occurrence to keep per key. Defaults to FIRST.", "FIRST"),
                f("skipBlankKeys", "boolean", false, "Skip items whose key is blank instead of deduplicating them together.", true)
        ));
        configs.put(AutomationNodeType.REMOVE_DUPLICATES, List.of(
                f("field", "string", true, "Field used to detect duplicate items.", "email"),
                f("keep", "string", false, "FIRST or LAST occurrence to keep. Defaults to FIRST.", "FIRST")
        ));
        configs.put(AutomationNodeType.CODE, List.of(
                f("expression", "string", false, "Single expression evaluated for its return value. Alternative to code.", "input.amount * 1.09"),
                f("code", "string", false, "Multi-statement script body. Alternative to expression.", "return { total: input.amount * 1.09 };"),
                f("language", "string", false, "Script language. Defaults to JAVASCRIPT.", "JAVASCRIPT"),
                f("mode", "string", false, "RUN_ONCE_FOR_ALL_ITEMS or RUN_ONCE_FOR_EACH_ITEM.", "RUN_ONCE_FOR_EACH_ITEM"),
                f("runnerUrl", "string", false, "Override URL of the sandboxed code runner. Uses the platform default when omitted.", null),
                f("targetPath", "string", false, "Variable path to store the script's return value at.", "codeResult")
        ));
        configs.put(AutomationNodeType.EXECUTION_DATA, List.of(
                f("data", "object", true, "Static data to inject into the flow's variables at this point.", Map.of())
        ));
        configs.put(AutomationNodeType.RESPOND_TO_WEBHOOK, List.of(
                f("statusCode", "integer", false, "HTTP status code returned to the webhook caller. Defaults to 200.", 200),
                f("headers", "object", false, "Map of response header name -> value.", Map.of("Content-Type", "application/json")),
                f("body", "object|array|string", false, "Response body sent back to the webhook caller.", Map.of("ok", true))
        ));
        configs.put(AutomationNodeType.STOP_AND_ERROR, List.of(
                f("message", "string", true, "Error message the flow fails with.", "Required field missing")
        ));
        configs.put(AutomationNodeType.NO_OP, List.of());
        configs.put(AutomationNodeType.N8N_WORKFLOW, List.of(
                f("webhookUrl", "string", true, "Webhook URL of the external n8n workflow to call.", "https://n8n.example.test/webhook/abc"),
                f("method", "string", false, "HTTP method. Defaults to POST.", "POST"),
                f("headers", "object", false, "Map of HTTP header name -> value.", Map.of()),
                f("body", "object|array|string", false, "Request body sent to the n8n webhook.", Map.of()),
                f("responseMappings", "object", false, "Map of variable path -> response path.", Map.of()),
                f("storeResponseAt", "string", false, "Variable path to store the full response at.", "n8nResponse")
        ));
        configs.put(AutomationNodeType.END, List.of());
        return configs.entrySet().stream().map(entry -> {
            Map<String,Object> properties = new LinkedHashMap<>();
            entry.getValue().forEach(field -> properties.put(field.key(), Map.of("type", field.type())));
            return Map.<String,Object>ofEntries(
                    Map.entry("type", entry.getKey().name()),
                    Map.entry("labelKey", "automation.node." + entry.getKey().name().toLowerCase()),
                    Map.entry("category", category(entry.getKey())),
                    Map.entry("commonFields", COMMON.stream().map(MetadataFieldDescriptor::key).toList()),
                    Map.entry("configFields", entry.getValue().stream().map(MetadataFieldDescriptor::key).toList()),
                    Map.entry("configFieldsMetadata", entry.getValue()),
                    Map.entry("configSchema", Map.of("type", "object", "properties", properties)),
                    Map.entry("runtimeModes", runtimeModes(entry.getKey())),
                    Map.entry("credentialReferenceOnly", true)
            );
        }).toList();
    }

    private static List<MetadataFieldDescriptor> conditionFields() {
        return List.of(
                f("field", "string", true, "Variable path evaluated by the condition.", "payload.currentFormValues.age"),
                f("operator", "string", true, "EQ, NE, GT, GTE, LT, LTE, CONTAINS, IN, EXISTS, or similar.", "GT"),
                f("value", "any", false, "Comparison value; shape depends on the operator.", 18)
        );
    }

    private static MetadataFieldDescriptor f(String key, String type, boolean required, String description, Object example) {
        return new MetadataFieldDescriptor(key, type, required, description, example);
    }

    private String category(AutomationNodeType type) {
        if (type.name().endsWith("TRIGGER")) return "TRIGGERS";
        if (type == AutomationNodeType.AI_OPERATION) return "AI";
        if (Set.of(AutomationNodeType.CALL_API, AutomationNodeType.HTTP_REQUEST, AutomationNodeType.N8N_WORKFLOW).contains(type)) return "INTEGRATIONS";
        if (Set.of(AutomationNodeType.IF, AutomationNodeType.SWITCH, AutomationNodeType.MERGE).contains(type)) return "LOGIC";
        return "DATA_FLOW";
    }

    private List<String> runtimeModes(AutomationNodeType type) {
        return switch (type) {
            case HTTP_REQUEST, LOOP_OVER_ITEMS, EXECUTE_WORKFLOW, EDIT_FIELDS, FILTER, SPLIT_OUT, AGGREGATE, SORT, LIMIT,
                    REMOVE_DUPLICATES, EXECUTION_DATA, RESPOND_TO_WEBHOOK, STOP_AND_ERROR, NO_OP -> List.of("N8N_ITEMS");
            case JDM_DECISION, PAGINATED_CALL_API, RUN_BATCH_JOB -> List.of("VARIABLES");
            default -> List.of("VARIABLES", "N8N_ITEMS");
        };
    }

    public Map<String, Object> edges() {
        return Map.of(
                "fields", List.of("id", "fromNodeId", "fromPort", "toNodeId", "toPort"),
                "portRules", Map.ofEntries(
                        Map.entry("IF", List.of("true", "false")),
                        Map.entry("SWITCH", List.of("<case-name>", "default")),
                        Map.entry("WAIT_FOR_CALLBACK", List.of("callback")),
                        Map.entry("LOOP_OVER_ITEMS", List.of("loop", "done")),
                        Map.entry("END", List.of()),
                        Map.entry("default", List.of("unnamed"))
                )
        );
    }

    private List<MetadataFieldDescriptor> callApiParams(boolean async) {
        List<MetadataFieldDescriptor> params = new java.util.ArrayList<>(List.of(
                f("serviceKey", "string", false, "Target internal service key. Alternative to url.", "processor-service"),
                f("path", "string", false, "Target internal path, used together with serviceKey.", "/internal/processor/forms/submit"),
                f("url", "string", false, "Direct URL for external calls. Alternative to serviceKey+path.", "https://example.test/verify"),
                f("method", "string", false, "HTTP method. Defaults to POST.", "POST"),
                f("headers", "object", false, "Map of HTTP header name -> value.", Map.of("X-API-KEY", "secret")),
                f("body", "object|array|string", false, "Templated request body.", Map.of("nationalCode", "{{payload.currentFormValues.nationalCode}}")),
                f("responseMappings", "object", false, "Map of variable path -> response path.", Map.of("verified", "verified")),
                f("storeFullResponseAt", "string", false, "Variable path to store the full response at.", "operatorResults.verify")
        ));
        if (async) {
            params.addAll(List.of(
                    f("actionKey", "string", false, "Stable async action key.", "screening"),
                    f("correlationKey", "string", false, "Optional callback correlation key.", "{{payload.currentFormValues.nationalCode}}:screening"),
                    f("callbackResponseMappings", "object", false, "Map of variable path -> callback payload path.", Map.of("riskScore", "riskScore")),
                    f("callbackStoreFullResponseAt", "string", false, "Variable path to store the full callback payload at.", "operatorResults.screeningCallback")
            ));
        }
        return params;
    }
}
