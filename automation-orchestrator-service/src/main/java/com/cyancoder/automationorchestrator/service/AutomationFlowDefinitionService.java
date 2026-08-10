package com.cyancoder.automationorchestrator.service;

import com.cyancoder.automationorchestrator.domain.AutomationEdge;
import com.cyancoder.automationorchestrator.domain.AutomationFlowDefinition;
import com.cyancoder.automationorchestrator.domain.AutomationNode;
import com.cyancoder.automationorchestrator.domain.AutomationNodeType;
import com.cyancoder.automationorchestrator.repo.AutomationFlowDefinitionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class AutomationFlowDefinitionService {
    private final AutomationFlowDefinitionRepository repository;
    private final ObjectMapper objectMapper;

    public AutomationFlowDefinitionService(AutomationFlowDefinitionRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public AutomationFlowDefinition save(String tenantKey, String siteKey, AutomationFlowDefinition definition, String actor) {
        definition.setTenantKey(scope(tenantKey, "default"));
        definition.setSiteKey(scope(siteKey, "default"));
        if (definition.getVersion() == null || definition.getVersion() < 1) definition.setVersion(1);
        if (blank(definition.getLifecycleStatus())) definition.setLifecycleStatus("DRAFT");
        if (blank(definition.getEnvironment())) definition.setEnvironment("default");
        if (blank(definition.getRuntimeMode())) definition.setRuntimeMode("VARIABLES");
        if (blank(definition.getCreatedBy())) definition.setCreatedBy(actor);
        definition.setUpdatedAt(Instant.now());
        validate(definition);
        return decoded(repository.save(encoded(definition)));
    }

    public List<AutomationFlowDefinition> list(String tenantKey, String siteKey) {
        return repository.findAllByTenantKeyAndSiteKeyOrderByFlowKeyAscVersionDesc(scope(tenantKey, "default"), scope(siteKey, "default"))
                .stream().map(this::decoded).toList();
    }

    public List<AutomationFlowDefinition> activeScheduledCandidates() {
        return repository.findAllByActiveTrue().stream().map(this::decoded).toList();
    }

    public AutomationFlowDefinition saveScheduleState(AutomationFlowDefinition definition) {
        return decoded(repository.save(encoded(definition)));
    }

    public AutomationFlowDefinition get(String tenantKey, String siteKey, String flowKey, Integer version) {
        return decoded(repository.findFirstByTenantKeyAndSiteKeyAndFlowKeyAndVersion(scope(tenantKey, "default"), scope(siteKey, "default"), flowKey, version).orElseThrow());
    }

    public AutomationFlowDefinition active(String tenantKey, String siteKey, String flowKey) {
        return active(tenantKey, siteKey, flowKey, "default");
    }

    public AutomationFlowDefinition active(String tenantKey, String siteKey, String flowKey, String environment) {
        return decoded(repository.findFirstByTenantKeyAndSiteKeyAndFlowKeyAndEnvironmentAndActiveTrueOrderByVersionDesc(
                scope(tenantKey, "default"), scope(siteKey, "default"), flowKey, scope(environment, "default")
        ).orElseThrow());
    }

    public AutomationFlowDefinition lifecycle(String tenantKey, String siteKey, String flowKey, Integer version, String action, String actor, String targetEnvironment) {
        AutomationFlowDefinition definition = get(tenantKey, siteKey, flowKey, version);
        switch (action.toUpperCase(Locale.ROOT)) {
            case "SUBMIT" -> definition.setLifecycleStatus("PENDING_APPROVAL");
            case "APPROVE" -> { definition.setLifecycleStatus("APPROVED"); definition.setApprovedBy(actor); definition.setApprovedAt(Instant.now()); }
            case "ACTIVATE" -> {
                if (!Set.of("APPROVED", "ACTIVE").contains(definition.getLifecycleStatus().toUpperCase(Locale.ROOT))) throw new IllegalArgumentException("only approved flows can be activated");
                repository.findAllByTenantKeyAndSiteKeyAndFlowKeyAndEnvironmentAndActiveTrue(
                                definition.getTenantKey(), definition.getSiteKey(), definition.getFlowKey(), definition.getEnvironment())
                        .stream()
                        .filter(active -> !Objects.equals(active.getId(), definition.getId()))
                        .forEach(active -> {
                            active.setActive(false);
                            active.setLifecycleStatus("RETIRED");
                            active.setUpdatedAt(Instant.now());
                            repository.save(active);
                        });
                definition.setLifecycleStatus("ACTIVE"); definition.setActive(true); definition.setPublishedBy(actor); definition.setPublishedAt(Instant.now());
            }
            case "PROMOTE" -> {
                AutomationFlowDefinition promoted = objectMapper.convertValue(definition, AutomationFlowDefinition.class);
                promoted.setId(null);
                promoted.setVersion(repository.findFirstByTenantKeyAndSiteKeyAndFlowKeyOrderByVersionDesc(
                                definition.getTenantKey(), definition.getSiteKey(), definition.getFlowKey())
                        .map(latest -> latest.getVersion() + 1)
                        .orElse(definition.getVersion() + 1));
                promoted.setEnvironment(scope(targetEnvironment, "default"));
                promoted.setLifecycleStatus("APPROVED");
                promoted.setActive(false);
                promoted.setApprovedBy(actor);
                promoted.setApprovedAt(Instant.now());
                promoted.setPublishedBy(null);
                promoted.setPublishedAt(null);
                promoted.setUpdatedAt(Instant.now());
                validate(promoted);
                return decoded(repository.save(encoded(promoted)));
            }
            default -> throw new IllegalArgumentException("unsupported lifecycle action: " + action);
        }
        definition.setUpdatedAt(Instant.now());
        return decoded(repository.save(encoded(definition)));
    }

    public void requireRoles(AutomationFlowDefinition definition, Set<String> actorRoles) {
        if (definition.getRequiredRoles() != null && !definition.getRequiredRoles().isEmpty()
                && (actorRoles == null || definition.getRequiredRoles().stream().noneMatch(actorRoles::contains))) {
            throw new IllegalArgumentException("actor lacks a role required by automation flow");
        }
    }

    public Map<String,Object> readiness(String tenantKey, String siteKey, String flowKey, Integer version) {
        AutomationFlowDefinition definition = get(tenantKey, siteKey, flowKey, version);
        List<String> errors = new java.util.ArrayList<>();
        try { validate(definition); } catch (IllegalArgumentException failure) { errors.add(failure.getMessage()); }
        String status = Objects.toString(definition.getLifecycleStatus(), "DRAFT").toUpperCase(Locale.ROOT);
        List<String> allowed = switch (status) {
            case "DRAFT" -> List.of("SUBMIT");
            case "PENDING_APPROVAL", "SUBMITTED" -> List.of("APPROVE");
            case "APPROVED" -> List.of("ACTIVATE", "PROMOTE");
            case "ACTIVE" -> List.of("PROMOTE");
            default -> List.of();
        };
        AutomationFlowDefinition active = repository.findFirstByTenantKeyAndSiteKeyAndFlowKeyAndEnvironmentAndActiveTrueOrderByVersionDesc(
                definition.getTenantKey(), definition.getSiteKey(), definition.getFlowKey(), definition.getEnvironment()).map(this::decoded).orElse(null);
        Map<String,Object> impact = new LinkedHashMap<>();
        impact.put("scheduleChanged", active != null && !Objects.equals(scheduleNode(active), scheduleNode(definition)));
        impact.put("webhookChanged", active != null && !Objects.equals(nodesOfType(active, AutomationNodeType.WEBHOOK_TRIGGER), nodesOfType(definition, AutomationNodeType.WEBHOOK_TRIGGER)));
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("valid", errors.isEmpty()); result.put("errors", errors); result.put("allowedActions", errors.isEmpty() ? allowed : List.of());
        result.put("activeVersion", active == null ? null : active.getVersion()); result.put("impact", impact);
        result.put("changed", active == null || !Objects.equals(objectMapper.convertValue(active, Map.class), objectMapper.convertValue(definition, Map.class)));
        return result;
    }

    private AutomationNode scheduleNode(AutomationFlowDefinition definition) { return definition.getNodes().stream().filter(n -> n.type() == AutomationNodeType.SCHEDULE_TRIGGER).findFirst().orElse(null); }
    private List<AutomationNode> nodesOfType(AutomationFlowDefinition definition, AutomationNodeType type) { return definition.getNodes().stream().filter(n -> n.type() == type).toList(); }

    public void validate(AutomationFlowDefinition definition) {
        if (blank(definition.getFlowKey())) throw new IllegalArgumentException("flowKey is required");
        if (!Set.of("VARIABLES", "N8N_ITEMS").contains(Objects.toString(definition.getRuntimeMode(), "VARIABLES").toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("runtimeMode must be VARIABLES or N8N_ITEMS");
        }
        if (blank(definition.getEntryNodeId())) throw new IllegalArgumentException("entryNodeId is required");
        if (definition.getNodes() == null || definition.getNodes().isEmpty()) throw new IllegalArgumentException("nodes are required");
        Set<String> ids = new HashSet<>();
        for (AutomationNode node : definition.getNodes()) {
            if (node == null || blank(node.id()) || node.type() == null) throw new IllegalArgumentException("every node requires id and type");
            if (!ids.add(node.id())) throw new IllegalArgumentException("duplicate node id: " + node.id());
        }
        AutomationNode entry = definition.getNodes().stream().filter(node -> definition.getEntryNodeId().equals(node.id())).findFirst().orElseThrow(() -> new IllegalArgumentException("entry node not found"));
        boolean itemRuntime = "N8N_ITEMS".equalsIgnoreCase(definition.getRuntimeMode());
        Set<AutomationNodeType> itemOnly = Set.of(
                AutomationNodeType.HTTP_REQUEST, AutomationNodeType.LOOP_OVER_ITEMS, AutomationNodeType.EXECUTE_WORKFLOW,
                AutomationNodeType.EDIT_FIELDS, AutomationNodeType.FILTER, AutomationNodeType.SPLIT_OUT,
                AutomationNodeType.AGGREGATE, AutomationNodeType.SORT, AutomationNodeType.LIMIT,
                AutomationNodeType.REMOVE_DUPLICATES, AutomationNodeType.EXECUTION_DATA,
                AutomationNodeType.RESPOND_TO_WEBHOOK, AutomationNodeType.STOP_AND_ERROR, AutomationNodeType.NO_OP
        );
        if (!itemRuntime) definition.getNodes().stream().filter(node -> itemOnly.contains(node.type())).findFirst().ifPresent(node -> {
            throw new IllegalArgumentException(node.type() + " requires runtimeMode=N8N_ITEMS");
        });
        Set<AutomationNodeType> itemTriggers = Set.of(
                AutomationNodeType.WEBHOOK_TRIGGER,
                AutomationNodeType.MANUAL_TRIGGER,
                AutomationNodeType.SCHEDULE_TRIGGER,
                AutomationNodeType.ERROR_TRIGGER
        );
        if (!itemTriggers.contains(entry.type())) {
            throw new IllegalArgumentException("entry node must be a trigger");
        }
        for (AutomationEdge edge : definition.getEdges() == null ? List.<AutomationEdge>of() : definition.getEdges()) {
            if (edge == null || !ids.contains(edge.fromNodeId()) || !ids.contains(edge.toNodeId())) throw new IllegalArgumentException("edge references an unknown node");
        }
        validateNodeContracts(definition);
    }

    private void validateNodeContracts(AutomationFlowDefinition definition) {
        List<AutomationEdge> edges = definition.getEdges() == null ? List.of() : definition.getEdges();
        for (AutomationNode node : definition.getNodes()) {
            Map<String,Object> config = node.configOrEmpty();
            List<AutomationEdge> outgoing = edges.stream().filter(edge -> node.id().equals(edge.fromNodeId())).toList();
            switch (node.type()) {
                case SCHEDULE_TRIGGER -> {
                    if (config.get("cron") == null && config.get("cronExpression") == null
                            && config.get("intervalSeconds") == null && config.get("rule") == null) {
                        throw new IllegalArgumentException("SCHEDULE_TRIGGER requires cron, intervalSeconds, or rule: " + node.id());
                    }
                }
                case END -> { if (!outgoing.isEmpty()) throw new IllegalArgumentException("END node cannot have outgoing edges: " + node.id()); }
                case IF -> { required(config,"field",node); required(config,"operator",node); requirePorts(outgoing,node,"true","false"); }
                case FILTER -> { required(config,"field",node); required(config,"operator",node); }
                case SWITCH -> { required(config,"field",node); required(config,"cases",node); for(String port:configMap(config.get("cases")).keySet()) if(outgoing.stream().noneMatch(edge->port.equals(edge.fromPort()))) throw new IllegalArgumentException("SWITCH node lacks edge for port " + port); }
                case WAIT_FOR_CALLBACK -> requirePorts(outgoing,node,"callback");
                case CALL_API, HTTP_REQUEST -> { if(config.get("url")==null&&(config.get("serviceKey")==null||config.get("path")==null)) throw new IllegalArgumentException(node.type()+" requires url or serviceKey/path"); }
                case N8N_WORKFLOW -> required(config,"webhookUrl",node);
                case PAGINATED_CALL_API -> { required(config,"itemsPath",node); if(config.get("url")==null&&(config.get("serviceKey")==null||config.get("path")==null)) throw new IllegalArgumentException("PAGINATED_CALL_API requires url or serviceKey/path"); }
                case RUN_BATCH_JOB -> required(config, "definitionKey", node);
                case AI_OPERATION -> {
                    required(config, "operation", node);
                    required(config, "instructions", node);
                    String operation = Objects.toString(config.get("operation"), "").toUpperCase(Locale.ROOT);
                    if (!Set.of("TRANSFORM_DATA", "GENERATE_CONTENT", "GENERATE_DSL").contains(operation))
                        throw new IllegalArgumentException("AI_OPERATION operation is unsupported: " + operation);
                }
                case FOR_EACH -> {
                    if ("N8N_ITEMS".equalsIgnoreCase(definition.getRuntimeMode())) {
                        requirePorts(outgoing,node,"loop","done");
                        long incoming = edges.stream().filter(edge -> node.id().equals(edge.toNodeId())).count();
                        if (incoming < 2) throw new IllegalArgumentException("FOR_EACH requires a feedback edge in N8N_ITEMS mode: " + node.id());
                    } else required(config,"sourcePath",node);
                }
                case LOOP_OVER_ITEMS -> {
                    requirePorts(outgoing,node,"loop","done");
                    long incoming = edges.stream().filter(edge -> node.id().equals(edge.toNodeId())).count();
                    if (incoming < 2) throw new IllegalArgumentException("LOOP_OVER_ITEMS requires a feedback edge from the loop branch: " + node.id());
                }
                case SUBFLOW, EXECUTE_WORKFLOW -> required(config,"flowKey",node);
                case JDM_DECISION -> { if(config.get("jdm")==null&&config.get("classpathResource")==null&&config.get("filePath")==null) throw new IllegalArgumentException("JDM_DECISION requires jdm, classpathResource, or filePath"); }
                case MAP_FIELDS -> required(config,"mappings",node);
                case EDIT_FIELDS -> { if (config.get("assignments") == null && config.get("mappings") == null) throw new IllegalArgumentException("EDIT_FIELDS node requires assignments: " + node.id()); }
                case FILE_METADATA -> required(config,"sourcePath",node);
                case DEDUP_BY_KEY -> { required(config,"sourcePath",node); required(config,"keyPath",node); }
                case REMOVE_DUPLICATES -> { if(config.get("field") == null && config.get("keyPath") == null) throw new IllegalArgumentException("REMOVE_DUPLICATES node requires field: " + node.id()); }
                case SPLIT_OUT -> { if(config.get("field") == null && config.get("sourcePath") == null) throw new IllegalArgumentException("SPLIT_OUT node requires field: " + node.id()); }
                case SORT -> required(config,"field",node);
                case LIMIT -> { if(config.get("maxItems") == null && config.get("limit") == null) throw new IllegalArgumentException("LIMIT node requires maxItems: " + node.id()); }
                case CODE -> { if(config.get("expression") == null && config.get("code") == null) throw new IllegalArgumentException("CODE node requires expression or code: " + node.id()); }
                default -> { }
            }
        }
    }
    private void required(Map<String,Object> config,String key,AutomationNode node){if(config.get(key)==null)throw new IllegalArgumentException(node.type()+" node requires "+key+": "+node.id());}
    private void requirePorts(List<AutomationEdge> edges,AutomationNode node,String...ports){for(String port:ports)if(edges.stream().noneMatch(edge->port.equals(edge.fromPort())))throw new IllegalArgumentException(node.type()+" node lacks edge for port "+port+": "+node.id());}
    private Map<String,Object> configMap(Object value){Map<String,Object> result=new java.util.LinkedHashMap<>();if(value instanceof Map<?,?> map)map.forEach((key,item)->{if(key!=null)result.put(key.toString(),item);});return result;}

    private AutomationFlowDefinition encoded(AutomationFlowDefinition value) {
        Map<String, Object> map = objectMapper.convertValue(value, Map.class);
        return objectMapper.convertValue(AutomationMapCodec.mongoSafe(map), AutomationFlowDefinition.class);
    }

    private AutomationFlowDefinition decoded(AutomationFlowDefinition value) {
        Map<String, Object> map = objectMapper.convertValue(value, Map.class);
        return objectMapper.convertValue(AutomationMapCodec.restore(map), AutomationFlowDefinition.class);
    }

    private String scope(String value, String fallback) { return blank(value) ? fallback : value.trim(); }
    private boolean blank(String value) { return value == null || value.isBlank(); }
}
