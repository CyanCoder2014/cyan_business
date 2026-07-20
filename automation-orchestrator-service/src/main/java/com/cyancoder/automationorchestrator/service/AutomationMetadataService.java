package com.cyancoder.automationorchestrator.service;

import com.cyancoder.automationorchestrator.domain.AutomationNodeType;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AutomationMetadataService {
    private static final List<String> COMMON = List.of("id", "name", "enabled", "credentialRef", "retryPolicy", "timeoutPolicy", "errorPolicy", "concurrencyPolicy", "config");

    public List<Map<String, Object>> nodes() {
        Map<AutomationNodeType, List<String>> configs = new LinkedHashMap<>();
        configs.put(AutomationNodeType.WEBHOOK_TRIGGER, List.of());
        configs.put(AutomationNodeType.WAIT, List.of("delaySeconds", "resumeAt"));
        configs.put(AutomationNodeType.WAIT_FOR_CALLBACK, List.of("callbackMappings", "callbackStorePath"));
        configs.put(AutomationNodeType.CALL_API, List.of("url|serviceKey+path", "method", "headers", "body", "responseMappings", "storeResponseAt"));
        configs.put(AutomationNodeType.PAGINATED_CALL_API, List.of("url|serviceKey+path", "method", "headers", "body", "pageStart", "pageEnd|pageCount", "pageParamPath", "size", "sizeParamPath", "itemsPath", "targetPath", "pageResponsesPath", "stopOnEmpty", "responseMappings"));
        configs.put(AutomationNodeType.IF, List.of("field", "operator", "value"));
        configs.put(AutomationNodeType.SWITCH, List.of("field", "cases"));
        configs.put(AutomationNodeType.MERGE, List.of());
        configs.put(AutomationNodeType.FOR_EACH, List.of("sourcePath", "chunkSize", "itemTemplate", "targetPath"));
        configs.put(AutomationNodeType.SUBFLOW, List.of("flowKey", "resultPath"));
        configs.put(AutomationNodeType.JDM_DECISION, List.of("jdm", "classpathResource", "filePath", "inputPath", "inputTemplate", "outputPath", "trace", "tracePath", "performancePath"));
        configs.put(AutomationNodeType.MAP_FIELDS, List.of("mappings"));
        configs.put(AutomationNodeType.JSON_TRANSFORM, List.of("sourcePath", "template", "targetPath"));
        configs.put(AutomationNodeType.FILE_METADATA, List.of("sourcePath", "targetPath"));
        configs.put(AutomationNodeType.DEDUP_BY_KEY, List.of("sourcePath", "keyPath", "targetPath", "keep", "skipBlankKeys"));
        configs.put(AutomationNodeType.CODE, List.of("expression", "targetPath"));
        configs.put(AutomationNodeType.N8N_WORKFLOW, List.of("webhookUrl", "method", "headers", "body", "responseMappings", "storeResponseAt"));
        configs.put(AutomationNodeType.END, List.of());
        return configs.entrySet().stream().map(entry -> Map.<String, Object>of(
                "type", entry.getKey().name(),
                "commonFields", COMMON,
                "configFields", entry.getValue()
        )).toList();
    }

    public Map<String, Object> edges() {
        return Map.of(
                "fields", List.of("id", "fromNodeId", "fromPort", "toNodeId"),
                "portRules", Map.ofEntries(
                        Map.entry("IF", List.of("true", "false")),
                        Map.entry("SWITCH", List.of("<case-name>", "default")),
                        Map.entry("WAIT_FOR_CALLBACK", List.of("callback")),
                        Map.entry("END", List.of()),
                        Map.entry("default", List.of("unnamed"))
                )
        );
    }
}
