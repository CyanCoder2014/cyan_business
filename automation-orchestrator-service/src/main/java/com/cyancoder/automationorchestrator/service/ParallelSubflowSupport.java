package com.cyancoder.automationorchestrator.service;

import com.cyancoder.automationorchestrator.domain.*;
import com.cyancoder.automationorchestrator.repo.AutomationExecutionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

/** Generic durable fan-out/join. Every branch receives a saved execution identity before it can be claimed. */
final class ParallelSubflowSupport {
    private static final Set<String> ACTIVE = Set.of("RUNNING", "WAITING", "WAITING_CALLBACK", "WAITING_CONCURRENCY", "CLAIMED");
    private static final Set<String> TERMINAL = Set.of("COMPLETED", "FAILED", "CANCELLED", "TIMED_OUT");

    private ParallelSubflowSupport() { }

    static void validate(Map<String, Object> config) {
        Map<String, Object> branches = AutomationDataSupport.map(config.get("branches"));
        if (branches.isEmpty() || branches.size() > 32) throw new IllegalArgumentException("PARALLEL_SUBFLOWS requires 1..32 named branches");
        for (var branch : branches.entrySet()) {
            if (!branch.getKey().matches("[A-Za-z][A-Za-z0-9_-]*")) throw new IllegalArgumentException("invalid parallel branch name");
            if (AutomationDataSupport.string(AutomationDataSupport.map(branch.getValue()).get("flowKey")) == null) throw new IllegalArgumentException("parallel branch requires flowKey");
        }
        long max = AutomationDataSupport.longValue(config.get("maxConcurrency"), branches.size());
        if (max < 1 || max > 32) throw new IllegalArgumentException("maxConcurrency must be 1..32");
    }

    static Result advance(AutomationExecution parent, AutomationNode node, AutomationFlowDefinitionService flows,
                          AutomationExecutionRepository executions, ObjectMapper mapper,
                          AutomationExecutionCheckpointService checkpoints) {
        Map<String, Object> config = node.configOrEmpty();
        validate(config);
        String marker = node.id().replace('.', '\uFF0E');
        Map<String, Object> all = AutomationDataSupport.map(parent.getContext().get("parallelSubflows"));
        Map<String, Object> state = AutomationDataSupport.map(all.get(marker));
        Map<String, Object> children = AutomationDataSupport.map(state.get("children"));
        if (children.isEmpty()) {
            String environment = Objects.toString(parent.getContext().getOrDefault("environment", "default"));
            for (var branch : AutomationDataSupport.map(config.get("branches")).entrySet()) {
                Map<String, Object> spec = AutomationDataSupport.map(branch.getValue());
                AutomationFlowDefinition definition = flows.active(parent.getTenantKey(), parent.getSiteKey(), Objects.toString(spec.get("flowKey")), environment);
                String stable = parent.getExecutionId() + ":" + node.id() + ":" + branch.getKey();
                String executionId = "exec-" + UUID.nameUUIDFromBytes(stable.getBytes(StandardCharsets.UTF_8));
                children.put(branch.getKey(), new LinkedHashMap<>(Map.of("executionId", executionId,
                        "flowKey", definition.getFlowKey(), "definition", AutomationMapCodec.mongoSafe(mapper.convertValue(definition, Map.class)),
                        "input", AutomationDataSupport.materialize(spec.getOrDefault("input", Map.of()), parent.getOutput(), parent.getContext()))));
            }
            state.put("children", children);
            state.put("createdAt", Instant.now().toString());
            all.put(marker, state);
            parent.getContext().put("parallelSubflows", all);
            if (checkpoints != null) checkpoints.checkpoint(parent); // launch plan survives a crash before child insert
        }
        int running = 0;
        for (Object item : children.values()) {
            var existing = executions.findByExecutionId(Objects.toString(AutomationDataSupport.map(item).get("executionId")));
            if (existing.isPresent() && ACTIVE.contains(existing.get().getStatus())) running++;
        }
        int limit = (int) AutomationDataSupport.longValue(config.get("maxConcurrency"), children.size());
        Map<String, Object> outputs = new LinkedHashMap<>();
        List<String> failures = new ArrayList<>();
        for (var branch : children.entrySet()) {
            Map<String, Object> childSpec = AutomationDataSupport.map(branch.getValue());
            String id = Objects.toString(childSpec.get("executionId"));
            AutomationExecution child = executions.findByExecutionId(id).orElse(null);
            if (child == null && running < limit) {
                child = child(parent, childSpec);
                try { executions.save(child); } catch (DuplicateKeyException duplicate) { child = executions.findByExecutionId(id).orElseThrow(); }
                running++;
            }
            if (child != null && "COMPLETED".equals(child.getStatus())) outputs.put(branch.getKey(), child.getOutput());
            else if (child != null && TERMINAL.contains(child.getStatus())) failures.add(branch.getKey() + "=" + child.getStatus());
        }
        if (!failures.isEmpty()) throw new IllegalStateException("parallel subflow failure: " + String.join(",", failures));
        if (outputs.size() == children.size()) {
            all.remove(marker);
            if (all.isEmpty()) parent.getContext().remove("parallelSubflows"); else parent.getContext().put("parallelSubflows", all);
            return new Result(true, outputs, Map.of("branchCount", children.size(), "status", "COMPLETED"));
        }
        return new Result(false, Map.of(), Map.of("branchCount", children.size(), "completed", outputs.size(), "running", running));
    }

    private static AutomationExecution child(AutomationExecution parent, Map<String, Object> spec) {
        Map<String, Object> definition = AutomationDataSupport.map(spec.get("definition"));
        AutomationExecution child = new AutomationExecution();
        child.setExecutionId(Objects.toString(spec.get("executionId")));
        child.setIdempotencyKey(child.getExecutionId());
        child.setBlockKey(Objects.toString(spec.get("flowKey")));
        child.setAutomationFlowKey(Objects.toString(spec.get("flowKey")));
        child.setFlowVersion(((Number) definition.get("version")).intValue());
        child.setEntryType("PARALLEL_SUBFLOW");
        child.setExecutionMode(AutomationExecutionMode.ASYNC);
        child.setFailurePolicy(parent.getFailurePolicy());
        child.setTenantKey(parent.getTenantKey()); child.setSiteKey(parent.getSiteKey()); child.setParentExecutionId(parent.getExecutionId());
        child.setInput(new LinkedHashMap<>(AutomationDataSupport.map(spec.get("input")))); child.setOutput(new LinkedHashMap<>(child.getInput()));
        child.setContext(Map.of("environment", parent.getContext().getOrDefault("environment", "default")));
        child.setInlineFragment(definition); child.setCurrentNodeId(Objects.toString(definition.get("entryNodeId")));
        child.setStatus("WAITING"); child.setResumeAt(Instant.now()); child.setResumeNodeId(child.getCurrentNodeId());
        child.setCreatedAt(Instant.now()); child.setUpdatedAt(Instant.now());
        return child;
    }

    record Result(boolean complete, Map<String, Object> outputs, Map<String, Object> details) { }
}
