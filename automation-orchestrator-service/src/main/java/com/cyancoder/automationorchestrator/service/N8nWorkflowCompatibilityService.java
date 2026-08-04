package com.cyancoder.automationorchestrator.service;

import com.cyancoder.automationorchestrator.domain.AutomationEdge;
import com.cyancoder.automationorchestrator.domain.AutomationFlowDefinition;
import com.cyancoder.automationorchestrator.domain.AutomationNode;
import com.cyancoder.automationorchestrator.domain.AutomationNodeType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class N8nWorkflowCompatibilityService {
    private static final Map<String, AutomationNodeType> TYPES = Map.ofEntries(
            Map.entry("manualtrigger", AutomationNodeType.MANUAL_TRIGGER),
            Map.entry("webhook", AutomationNodeType.WEBHOOK_TRIGGER),
            Map.entry("formtrigger", AutomationNodeType.WEBHOOK_TRIGGER),
            Map.entry("scheduletrigger", AutomationNodeType.SCHEDULE_TRIGGER),
            Map.entry("cron", AutomationNodeType.SCHEDULE_TRIGGER),
            Map.entry("errortrigger", AutomationNodeType.ERROR_TRIGGER),
            Map.entry("httprequest", AutomationNodeType.HTTP_REQUEST),
            Map.entry("set", AutomationNodeType.EDIT_FIELDS),
            Map.entry("editfields", AutomationNodeType.EDIT_FIELDS),
            Map.entry("if", AutomationNodeType.IF),
            Map.entry("switch", AutomationNodeType.SWITCH),
            Map.entry("filter", AutomationNodeType.FILTER),
            Map.entry("merge", AutomationNodeType.MERGE),
            Map.entry("splitout", AutomationNodeType.SPLIT_OUT),
            Map.entry("aggregate", AutomationNodeType.AGGREGATE),
            Map.entry("sort", AutomationNodeType.SORT),
            Map.entry("limit", AutomationNodeType.LIMIT),
            Map.entry("removeduplicates", AutomationNodeType.REMOVE_DUPLICATES),
            Map.entry("splitinbatches", AutomationNodeType.LOOP_OVER_ITEMS),
            Map.entry("executeWorkflow", AutomationNodeType.EXECUTE_WORKFLOW),
            Map.entry("executeworkflow", AutomationNodeType.EXECUTE_WORKFLOW),
            Map.entry("code", AutomationNodeType.CODE),
            Map.entry("function", AutomationNodeType.CODE),
            Map.entry("functionitem", AutomationNodeType.CODE),
            Map.entry("wait", AutomationNodeType.WAIT),
            Map.entry("respondtowebhook", AutomationNodeType.RESPOND_TO_WEBHOOK),
            Map.entry("executiondata", AutomationNodeType.EXECUTION_DATA),
            Map.entry("stopanderror", AutomationNodeType.STOP_AND_ERROR),
            Map.entry("noop", AutomationNodeType.NO_OP)
    );

    private final AutomationFlowDefinitionService definitions;

    public N8nWorkflowCompatibilityService(AutomationFlowDefinitionService definitions) {
        this.definitions = definitions;
    }

    public Map<String, Object> analyze(Map<String, Object> workflow) {
        List<Map<String, Object>> supported = new ArrayList<>();
        List<Map<String, Object>> unsupported = new ArrayList<>();
        for (Object raw : AutomationDataSupport.list(workflow.get("nodes"))) {
            Map<String, Object> node = AutomationDataSupport.map(raw);
            String sourceType = Objects.toString(node.get("type"), "");
            AutomationNodeType target = type(sourceType);
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("name", node.get("name"));
            entry.put("sourceType", sourceType);
            if (target == null) {
                entry.put("reason", "connector or node type has no native runtime implementation");
                unsupported.add(entry);
            } else {
                entry.put("targetType", target.name());
                supported.add(entry);
            }
        }
        return Map.of(
                "compatible", unsupported.isEmpty(),
                "runtimeMode", "N8N_ITEMS",
                "supportedNodes", supported,
                "unsupportedNodes", unsupported,
                "notes", List.of(
                        "Credentials are referenced, never imported with secrets",
                        "Expressions, item links, binary metadata, branching, merging, waits, retries and sub-workflows run natively",
                        "Community and app-specific n8n connectors require a platform connector implementation before import"
                )
        );
    }

    public AutomationFlowDefinition importAndSave(String tenant, String site, String requestedFlowKey,
                                                   Map<String, Object> workflow, String actor) {
        Map<String, Object> analysis = analyze(workflow);
        List<?> unsupported = (List<?>) analysis.get("unsupportedNodes");
        if (!unsupported.isEmpty()) throw new IllegalArgumentException("n8n workflow contains unsupported nodes: " + unsupported);
        return definitions.save(tenant, site, convert(requestedFlowKey, workflow), actor);
    }

    public AutomationFlowDefinition convert(String requestedFlowKey, Map<String, Object> workflow) {
        AutomationFlowDefinition result = new AutomationFlowDefinition();
        result.setFlowKey(nonBlank(requestedFlowKey, slug(Objects.toString(workflow.get("name"), "n8n-workflow"))));
        result.setName(Objects.toString(workflow.getOrDefault("name", result.getFlowKey())));
        result.setVersion(number(workflow.get("versionId"), 1));
        result.setRuntimeMode("N8N_ITEMS");
        result.setActive(Boolean.TRUE.equals(workflow.get("active")));
        result.setLifecycleStatus(result.isActive() ? "ACTIVE" : "DRAFT");
        result.setSettings(AutomationDataSupport.map(workflow.get("settings")));
        result.setPinData(AutomationDataSupport.map(workflow.get("pinData")));

        Map<String, String> idsByName = new LinkedHashMap<>();
        List<AutomationNode> nodes = new ArrayList<>();
        for (Object raw : AutomationDataSupport.list(workflow.get("nodes"))) {
            Map<String, Object> source = AutomationDataSupport.map(raw);
            String name = Objects.toString(source.getOrDefault("name", "Node"));
            String id = nonBlank(AutomationDataSupport.string(source.get("id")), slug(name) + "-" + UUID.randomUUID().toString().substring(0, 8));
            idsByName.put(name, id);
            AutomationNodeType type = type(Objects.toString(source.get("type"), ""));
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("n8nType", source.get("type"));
            data.put("typeVersion", source.get("typeVersion"));
            data.put("webhookId", source.get("webhookId"));
            nodes.add(new AutomationNode(
                    id, type, name, !Boolean.TRUE.equals(source.get("disabled")), credentialRef(source),
                    null, null, null, null, normalizeConfig(type, AutomationDataSupport.map(source.get("parameters"))),
                    position(source.get("position")), data
            ));
        }
        result.setNodes(nodes);

        List<AutomationEdge> edges = importConnections(workflow, idsByName, nodes);
        result.setEdges(edges);
        Set<AutomationNodeType> triggerTypes = Set.of(AutomationNodeType.MANUAL_TRIGGER, AutomationNodeType.WEBHOOK_TRIGGER,
                AutomationNodeType.SCHEDULE_TRIGGER, AutomationNodeType.ERROR_TRIGGER);
        result.setEntryNodeId(nodes.stream().filter(node -> triggerTypes.contains(node.type())).map(AutomationNode::id)
                .findFirst().orElseThrow(() -> new IllegalArgumentException("n8n workflow requires a supported trigger node")));
        return result;
    }

    public Map<String, Object> export(AutomationFlowDefinition definition) {
        List<Map<String, Object>> nodes = definition.getNodes().stream().map(node -> {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("id", node.id());
            result.put("name", node.name());
            result.put("type", sourceType(node));
            result.put("typeVersion", AutomationDataSupport.map(node.data()).getOrDefault("typeVersion", 1));
            result.put("position", List.of(
                    AutomationDataSupport.longValue(node.position() == null ? null : node.position().get("x"), 0),
                    AutomationDataSupport.longValue(node.position() == null ? null : node.position().get("y"), 0)
            ));
            result.put("parameters", node.configOrEmpty());
            if (!node.isEnabled()) result.put("disabled", true);
            return result;
        }).toList();
        Map<String, Object> connections = new LinkedHashMap<>();
        Map<String, AutomationNode> byId = new LinkedHashMap<>();
        definition.getNodes().forEach(node -> byId.put(node.id(), node));
        for (AutomationEdge edge : definition.getEdges()) {
            AutomationNode source = byId.get(edge.fromNodeId());
            AutomationNode target = byId.get(edge.toNodeId());
            if (source == null || target == null) continue;
            Map<String, Object> sourceConnections = AutomationDataSupport.map(connections.get(source.name()));
            List<Object> outputs = new ArrayList<>(AutomationDataSupport.list(sourceConnections.get("main")));
            int index = outputIndex(source.type(), edge.fromPort());
            while (outputs.size() <= index) outputs.add(new ArrayList<>());
            List<Object> targets = new ArrayList<>(AutomationDataSupport.list(outputs.get(index)));
            targets.add(Map.of("node", target.name(), "type", "main", "index", inputIndex(edge.toPort())));
            outputs.set(index, targets);
            sourceConnections.put("main", outputs);
            connections.put(source.name(), sourceConnections);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", definition.getName());
        result.put("active", definition.isActive());
        result.put("nodes", nodes);
        result.put("connections", connections);
        result.put("settings", definition.getSettings());
        result.put("pinData", definition.getPinData());
        result.put("meta", Map.of("source", "cyan-automation", "flowKey", definition.getFlowKey(), "version", definition.getVersion()));
        return result;
    }

    private List<AutomationEdge> importConnections(Map<String, Object> workflow, Map<String, String> idsByName,
                                                   List<AutomationNode> nodes) {
        Map<String, AutomationNodeType> typesByName = new LinkedHashMap<>();
        nodes.forEach(node -> typesByName.put(node.name(), node.type()));
        List<AutomationEdge> edges = new ArrayList<>();
        AutomationDataSupport.map(workflow.get("connections")).forEach((sourceName, rawConnections) -> {
            List<Object> outputs = AutomationDataSupport.list(AutomationDataSupport.map(rawConnections).get("main"));
            for (int output = 0; output < outputs.size(); output++) {
                for (Object rawTarget : AutomationDataSupport.list(outputs.get(output))) {
                    Map<String, Object> target = AutomationDataSupport.map(rawTarget);
                    String targetName = Objects.toString(target.get("node"), "");
                    String sourceId = idsByName.get(sourceName);
                    String targetId = idsByName.get(targetName);
                    if (sourceId == null || targetId == null) throw new IllegalArgumentException("connection references unknown n8n node");
                    edges.add(new AutomationEdge("e-" + UUID.randomUUID(), sourceId,
                            port(typesByName.get(sourceName), output), targetId,
                            Objects.toString(target.getOrDefault("index", 0))));
                }
            }
        });
        return edges;
    }

    private Map<String, Object> normalizeConfig(AutomationNodeType type, Map<String, Object> source) {
        Map<String, Object> config = new LinkedHashMap<>(source);
        if (type == AutomationNodeType.HTTP_REQUEST) {
            config.putIfAbsent("method", "GET");
            Map<String, Object> headers = nameValueMap(AutomationDataSupport.map(config.get("headerParameters")).get("parameters"));
            if (!headers.isEmpty()) config.put("headers", headers);
            Map<String, Object> body = nameValueMap(AutomationDataSupport.map(config.get("bodyParameters")).get("parameters"));
            if (!body.isEmpty()) config.put("body", body);
            Map<String, Object> query = nameValueMap(AutomationDataSupport.map(config.get("queryParameters")).get("parameters"));
            if (!query.isEmpty()) config.put("query", query);
        } else if (type == AutomationNodeType.LOOP_OVER_ITEMS) {
            rename(config, "batchSize", "batchSize");
        } else if (type == AutomationNodeType.CODE) {
            if (config.get("jsCode") != null) { config.put("code", config.remove("jsCode")); config.put("language", "javascript"); }
            if (config.get("pythonCode") != null) { config.put("code", config.remove("pythonCode")); config.put("language", "python"); }
        } else if (type == AutomationNodeType.EDIT_FIELDS) {
            Map<String, Object> assignments = new LinkedHashMap<>();
            Map<String, Object> assignmentUi = AutomationDataSupport.map(config.get("assignments"));
            AutomationDataSupport.list(assignmentUi.get("assignments")).forEach(raw -> {
                Map<String, Object> value = AutomationDataSupport.map(raw);
                if (value.get("name") != null) assignments.put(value.get("name").toString(), value.get("value"));
            });
            AutomationDataSupport.map(config.get("values")).values().forEach(group -> AutomationDataSupport.list(group).forEach(raw -> {
                Map<String, Object> value = AutomationDataSupport.map(raw);
                if (value.get("name") != null) assignments.put(value.get("name").toString(), value.get("value"));
            }));
            if (!assignments.isEmpty()) config.put("assignments", assignments);
            if ("keepOnlySet".equals(config.get("options"))) config.put("keepOnlySet", true);
        } else if (type == AutomationNodeType.IF || type == AutomationNodeType.FILTER) {
            normalizeCondition(config, firstCondition(config));
        } else if (type == AutomationNodeType.SWITCH) {
            List<Object> rules = AutomationDataSupport.list(AutomationDataSupport.map(config.get("rules")).get("values"));
            Map<String, Object> cases = new LinkedHashMap<>();
            for (int index = 0; index < rules.size(); index++) {
                Map<String, Object> rule = AutomationDataSupport.map(rules.get(index));
                Map<String, Object> condition = firstCondition(rule);
                if (index == 0) config.put("field", condition.get("leftValue"));
                cases.put(Integer.toString(index), condition.get("rightValue"));
            }
            if (!cases.isEmpty()) config.put("cases", cases);
        } else if (type == AutomationNodeType.MERGE) {
            String mode = Objects.toString(config.getOrDefault("mode", "append")).toUpperCase(Locale.ROOT);
            if ("COMBINE".equals(mode)) mode = "position".equalsIgnoreCase(Objects.toString(config.get("combineBy")))
                    ? "COMBINE_BY_POSITION" : "COMBINE_BY_FIELD";
            if ("CHOOSEBRANCH".equals(mode)) mode = "CHOOSE_BRANCH";
            config.put("mode", mode);
            Object fields = config.get("fieldsToMatchString");
            if (fields != null) config.put("field", fields.toString().split(",")[0].trim());
        } else if (type == AutomationNodeType.SPLIT_OUT) {
            rename(config, "fieldToSplitOut", "field");
            rename(config, "destinationFieldName", "targetField");
        } else if (type == AutomationNodeType.SORT) {
            List<Object> fields = AutomationDataSupport.list(AutomationDataSupport.map(config.get("sortFieldsUi")).get("sortField"));
            if (!fields.isEmpty()) {
                Map<String, Object> field = AutomationDataSupport.map(fields.getFirst());
                config.put("field", field.get("fieldName"));
                config.put("direction", field.getOrDefault("order", "ASC"));
            }
        } else if (type == AutomationNodeType.REMOVE_DUPLICATES) {
            Object fields = config.get("compare");
            if (fields instanceof String value && !value.isBlank()) config.put("field", value.split(",")[0].trim());
        } else if (type == AutomationNodeType.WAIT) {
            long amount = AutomationDataSupport.longValue(config.get("amount"), 1);
            String unit = Objects.toString(config.getOrDefault("unit", "seconds")).toLowerCase(Locale.ROOT);
            long multiplier = switch (unit) { case "minutes" -> 60; case "hours" -> 3600; case "days" -> 86400; default -> 1; };
            if (config.get("amount") != null) config.put("delaySeconds", amount * multiplier);
        } else if (type == AutomationNodeType.EXECUTE_WORKFLOW) {
            Object workflowId = config.get("workflowId");
            if (workflowId instanceof Map<?, ?> map) workflowId = AutomationDataSupport.map(map).get("value");
            if (workflowId != null) config.put("flowKey", workflowId.toString());
        } else if (type == AutomationNodeType.RESPOND_TO_WEBHOOK) {
            rename(config, "responseCode", "statusCode");
            rename(config, "responseBody", "body");
        }
        return config;
    }

    private Map<String, Object> firstCondition(Map<String, Object> config) {
        return AutomationDataSupport.map(AutomationDataSupport.list(
                AutomationDataSupport.map(config.get("conditions")).get("conditions")).stream().findFirst().orElse(Map.of()));
    }

    private void normalizeCondition(Map<String, Object> config, Map<String, Object> condition) {
        if (condition.isEmpty()) return;
        config.put("field", condition.get("leftValue"));
        config.put("value", condition.get("rightValue"));
        String operation = Objects.toString(AutomationDataSupport.map(condition.get("operator")).getOrDefault("operation", "equals"));
        config.put("operator", switch (operation.toLowerCase(Locale.ROOT)) {
            case "notequals", "not_equal" -> "NE";
            case "larger", "gt" -> "GT";
            case "largerequal", "gte" -> "GTE";
            case "smaller", "lt" -> "LT";
            case "smallerequal", "lte" -> "LTE";
            case "contains" -> "CONTAINS";
            case "empty", "isempty" -> "EMPTY";
            case "notempty", "isnotempty" -> "NOT_EMPTY";
            default -> "EQ";
        });
    }

    private Map<String, Object> nameValueMap(Object raw) {
        Map<String, Object> result = new LinkedHashMap<>();
        AutomationDataSupport.list(raw).forEach(entry -> {
            Map<String, Object> value = AutomationDataSupport.map(entry);
            if (value.get("name") != null) result.put(value.get("name").toString(), value.get("value"));
        });
        return result;
    }

    private String credentialRef(Map<String, Object> source) {
        for (Object raw : AutomationDataSupport.map(source.get("credentials")).values()) {
            Map<String, Object> credential = AutomationDataSupport.map(raw);
            String id = AutomationDataSupport.string(credential.get("id"));
            if (id != null) return id;
            String name = AutomationDataSupport.string(credential.get("name"));
            if (name != null) return name;
        }
        return null;
    }

    private Map<String, Object> position(Object raw) {
        List<Object> values = AutomationDataSupport.list(raw);
        if (values.size() < 2) return Map.of();
        return Map.of("x", values.get(0), "y", values.get(1));
    }

    private AutomationNodeType type(String sourceType) {
        String leaf = sourceType.substring(sourceType.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        return TYPES.get(leaf);
    }

    private String sourceType(AutomationNode node) {
        Object original = AutomationDataSupport.map(node.data()).get("n8nType");
        if (original != null) return original.toString();
        return switch (node.type()) {
            case MANUAL_TRIGGER -> "n8n-nodes-base.manualTrigger";
            case WEBHOOK_TRIGGER -> "n8n-nodes-base.webhook";
            case SCHEDULE_TRIGGER -> "n8n-nodes-base.scheduleTrigger";
            case ERROR_TRIGGER -> "n8n-nodes-base.errorTrigger";
            case HTTP_REQUEST, CALL_API -> "n8n-nodes-base.httpRequest";
            case EDIT_FIELDS, MAP_FIELDS -> "n8n-nodes-base.set";
            case LOOP_OVER_ITEMS, FOR_EACH -> "n8n-nodes-base.splitInBatches";
            case EXECUTE_WORKFLOW, SUBFLOW -> "n8n-nodes-base.executeWorkflow";
            default -> "n8n-nodes-base." + node.type().name().toLowerCase(Locale.ROOT).replace("_", "");
        };
    }

    private String port(AutomationNodeType type, int output) {
        if (type == AutomationNodeType.IF) return output == 0 ? "true" : "false";
        if (type == AutomationNodeType.LOOP_OVER_ITEMS || type == AutomationNodeType.FOR_EACH) return output == 0 ? "done" : "loop";
        if (type == AutomationNodeType.WAIT_FOR_CALLBACK) return "callback";
        return output == 0 ? "0" : Integer.toString(output);
    }

    private int outputIndex(AutomationNodeType type, String port) {
        if (type == AutomationNodeType.IF) return "false".equals(port) ? 1 : 0;
        if (type == AutomationNodeType.LOOP_OVER_ITEMS || type == AutomationNodeType.FOR_EACH) return "loop".equals(port) ? 1 : 0;
        try { return Integer.parseInt(Objects.toString(port, "0")); } catch (NumberFormatException ignored) { return 0; }
    }

    private int inputIndex(String port) {
        try { return Integer.parseInt(Objects.toString(port, "0")); } catch (NumberFormatException ignored) { return 0; }
    }

    private void rename(Map<String, Object> map, String source, String target) {
        if (!source.equals(target) && map.containsKey(source)) map.put(target, map.remove(source));
    }

    private int number(Object value, int fallback) { return value instanceof Number number ? number.intValue() : fallback; }
    private String nonBlank(String primary, String fallback) { return primary == null || primary.isBlank() ? fallback : primary; }
    private String slug(String value) { return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", ""); }
}
