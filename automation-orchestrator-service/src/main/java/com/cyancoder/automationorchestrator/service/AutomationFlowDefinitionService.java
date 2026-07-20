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
        if (blank(definition.getCreatedBy())) definition.setCreatedBy(actor);
        definition.setUpdatedAt(Instant.now());
        validate(definition);
        return decoded(repository.save(encoded(definition)));
    }

    public List<AutomationFlowDefinition> list(String tenantKey, String siteKey) {
        return repository.findAllByTenantKeyAndSiteKeyOrderByFlowKeyAscVersionDesc(scope(tenantKey, "default"), scope(siteKey, "default"))
                .stream().map(this::decoded).toList();
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

    public void validate(AutomationFlowDefinition definition) {
        if (blank(definition.getFlowKey())) throw new IllegalArgumentException("flowKey is required");
        if (blank(definition.getEntryNodeId())) throw new IllegalArgumentException("entryNodeId is required");
        if (definition.getNodes() == null || definition.getNodes().isEmpty()) throw new IllegalArgumentException("nodes are required");
        Set<String> ids = new HashSet<>();
        for (AutomationNode node : definition.getNodes()) {
            if (node == null || blank(node.id()) || node.type() == null) throw new IllegalArgumentException("every node requires id and type");
            if (!ids.add(node.id())) throw new IllegalArgumentException("duplicate node id: " + node.id());
        }
        AutomationNode entry = definition.getNodes().stream().filter(node -> definition.getEntryNodeId().equals(node.id())).findFirst().orElseThrow(() -> new IllegalArgumentException("entry node not found"));
        if (entry.type() != AutomationNodeType.WEBHOOK_TRIGGER) throw new IllegalArgumentException("entry node must be WEBHOOK_TRIGGER");
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
                case END -> { if (!outgoing.isEmpty()) throw new IllegalArgumentException("END node cannot have outgoing edges: " + node.id()); }
                case IF -> { required(config,"field",node); required(config,"operator",node); requirePorts(outgoing,node,"true","false"); }
                case SWITCH -> { required(config,"field",node); required(config,"cases",node); for(String port:configMap(config.get("cases")).keySet()) if(outgoing.stream().noneMatch(edge->port.equals(edge.fromPort()))) throw new IllegalArgumentException("SWITCH node lacks edge for port " + port); }
                case WAIT_FOR_CALLBACK -> requirePorts(outgoing,node,"callback");
                case CALL_API -> { if(config.get("url")==null&&(config.get("serviceKey")==null||config.get("path")==null)) throw new IllegalArgumentException("CALL_API requires url or serviceKey/path"); }
                case N8N_WORKFLOW -> required(config,"webhookUrl",node);
                case PAGINATED_CALL_API -> { required(config,"itemsPath",node); if(config.get("url")==null&&(config.get("serviceKey")==null||config.get("path")==null)) throw new IllegalArgumentException("PAGINATED_CALL_API requires url or serviceKey/path"); }
                case FOR_EACH -> required(config,"sourcePath",node);
                case SUBFLOW -> required(config,"flowKey",node);
                case JDM_DECISION -> { if(config.get("jdm")==null&&config.get("classpathResource")==null&&config.get("filePath")==null) throw new IllegalArgumentException("JDM_DECISION requires jdm, classpathResource, or filePath"); }
                case MAP_FIELDS -> required(config,"mappings",node);
                case FILE_METADATA -> required(config,"sourcePath",node);
                case DEDUP_BY_KEY -> { required(config,"sourcePath",node); required(config,"keyPath",node); }
                case CODE -> required(config,"expression",node);
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
