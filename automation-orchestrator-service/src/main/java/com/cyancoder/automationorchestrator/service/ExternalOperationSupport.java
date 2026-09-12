package com.cyancoder.automationorchestrator.service;

import com.cyancoder.automationorchestrator.domain.AutomationExecution;
import com.cyancoder.automationorchestrator.domain.AutomationNode;
import java.time.Instant;
import java.util.*;

/** A generic operation journal: persist IN_FLIGHT, reconcile first on every retry, then invoke idempotently. */
final class ExternalOperationSupport {
    private ExternalOperationSupport() { }
    static void validate(Map<String, Object> config, AutomationNode node) {
        if (AutomationDataSupport.string(config.get("operationKey")) == null) throw new IllegalArgumentException("EXTERNAL_OPERATION requires operationKey: " + node.id());
        if (AutomationDataSupport.map(config.get("execute")).isEmpty() || AutomationDataSupport.map(config.get("reconcile")).isEmpty())
            throw new IllegalArgumentException("EXTERNAL_OPERATION requires execute and reconcile configurations: " + node.id());
    }
    static Result execute(AutomationExecution execution, AutomationNode node, Map<String, Object> config,
                                       TriCaller caller, AutomationExecutionCheckpointService checkpoints) {
        String key = AutomationDataSupport.string(AutomationDataSupport.resolve(config.get("operationKey"), execution.getOutput(), execution.getContext()));
        if (key == null || key.isBlank()) throw new IllegalArgumentException("resolved operationKey is blank");
        Map<String, Object> journal = AutomationDataSupport.map(execution.getContext().get("externalOperations"));
        Map<String, Object> entry = AutomationDataSupport.map(journal.get(node.id().replace('.', '\uFF0E')));
        if (entry.isEmpty()) { entry = new LinkedHashMap<>(Map.of("operationKey", key, "status", "PREPARED", "preparedAt", Instant.now().toString())); journal.put(node.id().replace('.', '\uFF0E'), entry); execution.getContext().put("externalOperations", journal); if (checkpoints != null) checkpoints.checkpoint(execution); }
        Map<String, Object> reconcile = new LinkedHashMap<>(AutomationDataSupport.map(config.get("reconcile")));
        reconcile.put("body", withKey(reconcile.get("body"), key, execution));
        Map<String, Object> reconciled = caller.call(execution, node, reconcile);
        if (Boolean.TRUE.equals(AutomationDataSupport.readPath(reconciled, Objects.toString(config.getOrDefault("reconciledSuccessPath", "confirmed"))))) {
            entry.put("status", "CONFIRMED"); entry.put("result", reconciled); entry.put("confirmedAt", Instant.now().toString()); return new Result(reconciled, true);
        }
        entry.put("status", "IN_FLIGHT"); entry.put("lastAttemptAt", Instant.now().toString()); if (checkpoints != null) checkpoints.checkpoint(execution);
        Map<String, Object> invoke = new LinkedHashMap<>(AutomationDataSupport.map(config.get("execute")));
        invoke.put("body", withKey(invoke.get("body"), key, execution));
        String header = Objects.toString(config.getOrDefault("idempotencyHeader", "Idempotency-Key"));
        Map<String, Object> headers = new LinkedHashMap<>(AutomationDataSupport.map(invoke.get("headers"))); headers.put(header, key); invoke.put("headers", headers);
        Map<String, Object> result = caller.call(execution, node, invoke);
        boolean confirmed = Boolean.TRUE.equals(AutomationDataSupport.readPath(result, Objects.toString(config.getOrDefault("executeSuccessPath", "confirmed"))));
        entry.put("status", confirmed ? "CONFIRMED" : "SUBMITTED"); entry.put("result", result); entry.put("submittedAt", Instant.now().toString());
        if (confirmed) entry.put("confirmedAt", Instant.now().toString());
        return new Result(result, confirmed);
    }
    private static Map<String, Object> withKey(Object body, String key, AutomationExecution execution) {
        Map<String, Object> result = new LinkedHashMap<>(AutomationDataSupport.map(AutomationDataSupport.materialize(body == null ? Map.of() : body, execution.getOutput(), execution.getContext())));
        result.putIfAbsent("operationKey", key); return result;
    }
    @FunctionalInterface interface TriCaller { Map<String, Object> call(AutomationExecution execution, AutomationNode node, Map<String, Object> config); }
    record Result(Map<String, Object> response, boolean confirmed) { }
}
