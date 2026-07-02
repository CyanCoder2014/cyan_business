package com.cyancoder.bpm.service;

import com.cyancoder.bpm.api.dto.BpmScope;
import com.cyancoder.bpm.api.dto.TransitionActorContext;
import com.cyancoder.bpm.domain.ActionType;
import com.cyancoder.bpm.domain.AsyncActionRegistration;
import com.cyancoder.bpm.domain.AutomationBlockExecution;
import com.cyancoder.bpm.domain.AutomationExecutionMode;
import com.cyancoder.bpm.domain.AutomationFailurePolicy;
import com.cyancoder.bpm.domain.ConditionLogicalOperator;
import com.cyancoder.bpm.domain.ConditionOperator;
import com.cyancoder.bpm.domain.FlowAccessRule;
import com.cyancoder.bpm.domain.FlowActionConfig;
import com.cyancoder.bpm.domain.FlowCondition;
import com.cyancoder.bpm.domain.FlowTransition;
import com.cyancoder.bpm.domain.ManagedObject;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class FlowActionExecutor {
    private final DynamicFlowIntegrationClient integrationClient;
    private final FlowTransitionConditionEvaluator actionConditionEvaluator = new FlowTransitionConditionEvaluator();

    public FlowActionExecutor(DynamicFlowIntegrationClient integrationClient) {
        this.integrationClient = integrationClient;
    }

    public void execute(List<FlowActionConfig> actions, ManagedObject object, BpmScope scope, String actorUserId) {
        if (actions == null) {
            return;
        }
        for (FlowActionConfig action : actions) {
            if (action == null || action.type() == null) {
                continue;
            }
            if (!shouldExecute(action, object, actorUserId)) {
                continue;
            }
            try {
                apply(action, object, scope, actorUserId);
            } catch (RuntimeException ex) {
                if (continueOnError(action)) {
                    object.getAuditLog().add("action " + action.type() + " failed but continued: " + ex.getMessage());
                    continue;
                }
                throw ex;
            }
        }
    }

    private boolean shouldExecute(FlowActionConfig action, ManagedObject object, String actorUserId) {
        Map<String, Object> params = action.params() == null ? Map.of() : action.params();
        String expression = stringValue(params.get("whenExpression"));
        if (expression != null && !expression.isBlank()) {
            FlowTransition synthetic = new FlowTransition(
                    "__action_gate",
                    object.getState(),
                    object.getState(),
                    "Action gate",
                    Set.of(),
                    Set.of(),
                    expression,
                    ConditionLogicalOperator.AND,
                    List.of()
            );
            if (!actionConditionEvaluator.evaluate(synthetic, object.getPayload(), Map.of(), object, new TransitionActorContext(actorUserId, Set.of(), Set.of()))) {
                return false;
            }
        }
        String whenField = stringValue(params.get("whenField"));
        if (whenField == null || whenField.isBlank()) {
            return true;
        }
        ConditionOperator operator = parseConditionOperator(params.get("whenOperator"));
        FlowTransition synthetic = new FlowTransition(
                "__action_gate",
                object.getState(),
                object.getState(),
                "Action gate",
                Set.of(),
                Set.of(),
                null,
                ConditionLogicalOperator.AND,
                List.of(new FlowCondition(whenField, operator, params.get("whenValue")))
        );
        return actionConditionEvaluator.evaluate(synthetic, object.getPayload(), Map.of(), object, new TransitionActorContext(actorUserId, Set.of(), Set.of()));
    }

    private boolean continueOnError(FlowActionConfig action) {
        Map<String, Object> params = action.params() == null ? Map.of() : action.params();
        Object value = params.get("continueOnError");
        return value instanceof Boolean bool && bool
                || value instanceof String text && Boolean.parseBoolean(text);
    }

    private void apply(FlowActionConfig action, ManagedObject object, BpmScope scope, String actorUserId) {
        Map<String, Object> params = action.params() == null ? Map.of() : action.params();
        switch (action.type()) {
            case NONE -> { }
            case ADD_AUDIT_ENTRY -> object.getAuditLog().add(String.valueOf(params.getOrDefault("message", "audit")) + " by=" + actorUserId + " at=" + Instant.now());
            case SET_ASSIGNEE -> object.setAssignee(stringValue(params.get("assignee")));
            case SET_ACCESS_RULE -> object.setAccessRule(new FlowAccessRule(stringSet(params.get("canRead")), stringSet(params.get("canEdit")), stringSet(params.get("canApprove"))));
            case LOCK_OBJECT -> object.setLocked(true);
            case UNLOCK_OBJECT -> object.setLocked(false);
            case UPDATE_OBJECT_FIELDS -> updateFields(object.getPayload(), params);
            case COPY_FIELDS -> copyFields(object.getPayload(), params);
            case REMOVE_FIELDS -> removeFields(object.getPayload(), params);
            case CALL_API, CALL_OPERATOR -> syncCall(action, object, scope, actorUserId);
            case CALL_API_ASYNC, CALL_OPERATOR_ASYNC -> registerAsyncAction(action, object, scope, actorUserId);
            case RUN_AUTOMATION_BLOCK -> runAutomationBlock(action, object, scope, actorUserId);
            case NOTIFY_OWNER -> integrationClient.callAction(action, object, scope);
        }
    }

    private void syncCall(FlowActionConfig action, ManagedObject object, BpmScope scope, String actorUserId) {
        Map<String, Object> response = integrationClient.callActionForResponse(action, object, scope, actorUserId);
        Map<String, Object> params = action.params() == null ? Map.of() : action.params();
        CallApiActionSupport.applyResponseMappings(object, response, params.get("responseMappings"));
        Object storeFullResponseAt = params.get("storeFullResponseAt");
        if (storeFullResponseAt != null) {
            ActionPayloadSupport.setPayloadPath(object, storeFullResponseAt.toString(), response);
        }
    }

    private void registerAsyncAction(FlowActionConfig action, ManagedObject object, BpmScope scope, String actorUserId) {
        Map<String, Object> params = action.params() == null ? Map.of() : action.params();
        String actionKey = stringValue(params.getOrDefault("actionKey", "async-" + Instant.now().toEpochMilli()));
        String correlationKey = stringValue(CallApiActionSupport.resolveTemplate(params.getOrDefault("correlationKey", object.getId() + ":" + actionKey), object, actorUserId));
        AsyncActionRegistration registration = new AsyncActionRegistration();
        registration.setActionKey(actionKey);
        registration.setCorrelationKey(correlationKey);
        registration.setStateId(object.getState());
        registration.setStatus("PENDING");
        object.getAsyncActionRegistry().removeIf(existing -> actionKey.equals(existing.getActionKey()));
        object.getAsyncActionRegistry().add(registration);
        ActionPayloadSupport.setPayloadPath(object, "payload.asyncActions." + actionKey + ".status", "PENDING");
        ActionPayloadSupport.setPayloadPath(object, "payload.asyncActions." + actionKey + ".correlationKey", correlationKey);
        ActionPayloadSupport.setPayloadPath(object, "payload.asyncActions." + actionKey + ".actionType", action.type().name());
        if (params.get("callbackResponseMappings") != null) {
            ActionPayloadSupport.setPayloadPath(object, "payload.asyncActions." + actionKey + ".callbackResponseMappings", params.get("callbackResponseMappings"));
        }
        if (params.get("callbackStoreFullResponseAt") != null) {
            ActionPayloadSupport.setPayloadPath(object, "payload.asyncActions." + actionKey + ".callbackStoreFullResponseAt", params.get("callbackStoreFullResponseAt"));
        }
        if (!Boolean.FALSE.equals(params.get("fireAndForget"))) {
            Map<String, Object> response = integrationClient.callActionForResponse(action, object, scope, actorUserId);
            CallApiActionSupport.applyResponseMappings(object, response, params.get("responseMappings"));
            Object storeFullResponseAt = params.get("storeFullResponseAt");
            if (storeFullResponseAt != null) {
                ActionPayloadSupport.setPayloadPath(object, storeFullResponseAt.toString(), response);
            }
        }
    }

    private void runAutomationBlock(FlowActionConfig action, ManagedObject object, BpmScope scope, String actorUserId) {
        Map<String, Object> params = action.params() == null ? Map.of() : action.params();
        String blockKey = firstNonBlank(
                stringValue(params.get("blockKey")),
                stringValue(params.get("actionKey")),
                stringValue(params.get("flowKey")),
                stringValue(params.get("automationFlowKey")),
                "automation-" + Instant.now().toEpochMilli()
        );
        String correlationKey = stringValue(CallApiActionSupport.resolveTemplate(params.getOrDefault("correlationKey", object.getId() + ":" + blockKey), object, actorUserId));
        AutomationExecutionMode mode = parseMode(params.get("executionMode"), params.get("async"));
        AutomationFailurePolicy failurePolicy = parseFailurePolicy(params.get("failurePolicy"));
        Map<String, Object> blockInput = resolveMapTemplate(firstPresent(params, "body", "variables", "input"), object, actorUserId);
        Map<String, Object> blockContext = new LinkedHashMap<>(resolveMapTemplate(params.get("context"), object, actorUserId));
        blockContext.putIfAbsent("managedObjectId", object.getId());
        blockContext.putIfAbsent("flowKey", object.getFlowKey());
        blockContext.putIfAbsent("stateId", object.getState());
        blockContext.putIfAbsent("blockKey", blockKey);

        AutomationBlockExecution block = new AutomationBlockExecution();
        block.setBlockKey(blockKey);
        block.setAutomationFlowKey(firstNonBlank(stringValue(params.get("automationFlowKey")), stringValue(params.get("flowKey")), blockKey));
        block.setExecutionMode(mode);
        block.setFailurePolicy(failurePolicy);
        block.setStateId(object.getState());
        block.setCorrelationKey(correlationKey);
        block.setWaitForCompletion(Boolean.parseBoolean(String.valueOf(params.getOrDefault("waitForCompletion", mode == AutomationExecutionMode.ASYNC))));
        block.setStatus(mode == AutomationExecutionMode.SYNC ? "RUNNING" : "PENDING");
        block.setServiceKey(stringValue(params.getOrDefault("serviceKey", "automation-orchestrator-service")));
        block.setPath(stringValue(params.getOrDefault("path", "/internal/automation-orchestrator/executions/start")));
        block.setMethod(stringValue(params.getOrDefault("method", "POST")));
        block.setRequestBody(blockInput);
        block.setInlineFragment(resolveMapTemplate(firstPresent(params, "inlineFragment", "inlineFlow"), object, actorUserId));
        block.setStartResponseMappings(asStringObjectMap(firstPresent(params, "responseMappings", "startResponseMappings")));
        block.setStoreStartResponseAt(firstNonBlank(stringValue(params.get("storeFullResponseAt")), stringValue(params.get("storeStartResponseAt"))));
        block.setOutputMappings(asStringObjectMap(firstPresent(params, "callbackResponseMappings", "outputMappings", "resultMappings")));
        block.setStoreOutputAt(firstNonBlank(stringValue(params.get("callbackStoreFullResponseAt")), stringValue(params.get("storeOutputAt"))));
        block.setStoreExecutionIdAt(stringValue(params.get("storeExecutionIdAt")));
        block.setStoreStatusAt(stringValue(params.get("storeStatusAt")));
        block.setStoreVariablesAt(stringValue(params.get("storeVariablesAt")));
        block.setStoreFullExecutionAt(stringValue(params.get("storeFullExecutionAt")));
        block.setStoreErrorAt(stringValue(params.get("storeErrorAt")));
        block.setNextStateOnSuccess(stringValue(params.get("nextStateOnSuccess")));
        block.setNextStateOnFailure(stringValue(params.get("nextStateOnFailure")));
        block.setMaxRetries(intValue(params.get("maxRetries"), 0));
        block.setRetryCount(0);
        block.setTimeoutSeconds(longValue(params.get("timeoutSeconds")));
        block.setTimeoutAt(block.getTimeoutSeconds() == null ? null : Instant.now().plusSeconds(block.getTimeoutSeconds()));
        block.setStartedAt(Instant.now());
        block.setUpdatedAt(Instant.now());
        object.getAutomationBlockRegistry().removeIf(existing -> blockKey.equals(existing.getBlockKey()));
        object.getAutomationBlockRegistry().add(block);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("blockKey", blockKey);
        requestBody.put("automationFlowKey", block.getAutomationFlowKey());
        requestBody.put("flowKey", block.getAutomationFlowKey());
        requestBody.put("managedObjectId", object.getId());
        requestBody.put("idempotencyKey", stringValue(CallApiActionSupport.resolveTemplate(params.get("idempotencyKey"), object, actorUserId)));
        requestBody.put("executionMode", block.getExecutionMode().name());
        requestBody.put("failurePolicy", block.getFailurePolicy().name());
        requestBody.put("correlationKey", correlationKey);
        requestBody.put("callbackPath", mode == AutomationExecutionMode.ASYNC ? "/public/bpm/async-actions/callbacks/" + correlationKey : null);
        requestBody.put("tenantKey", scope.tenantKey());
        requestBody.put("siteKey", scope.siteKey());
        requestBody.put("input", block.getRequestBody());
        requestBody.put("variables", block.getRequestBody());
        requestBody.put("context", blockContext);
        requestBody.put("inlineFragment", block.getInlineFragment().isEmpty() ? null : block.getInlineFragment());
        requestBody.put("inlineFlow", block.getInlineFragment().isEmpty() ? null : block.getInlineFragment());
        requestBody.put("maxRetries", block.getMaxRetries());
        requestBody.put("timeoutSeconds", block.getTimeoutSeconds());
        requestBody.put("delayMillis", longValue(params.get("delayMillis")));

        FlowActionConfig requestAction = new FlowActionConfig(ActionType.RUN_AUTOMATION_BLOCK, Map.of(
                "serviceKey", block.getServiceKey(),
                "path", block.getPath(),
                "method", block.getMethod(),
                "body", requestBody
        ));
        Map<String, Object> response = integrationClient.callActionForResponse(requestAction, object, scope, actorUserId);
        applyAutomationStartResponse(object, block, response);

        if (mode == AutomationExecutionMode.SYNC) {
            finalizeSyncBlock(object, block, response);
        }
    }

    private void applyAutomationStartResponse(ManagedObject object, AutomationBlockExecution block, Map<String, Object> response) {
        CallApiActionSupport.applyResponseMappings(object, response, block.getStartResponseMappings());
        if (block.getStoreStartResponseAt() != null && !block.getStoreStartResponseAt().isBlank()) {
            ActionPayloadSupport.setPayloadPath(object, block.getStoreStartResponseAt(), response);
        }
        applyExecutionStores(object, block, response);
        block.setUpdatedAt(Instant.now());
        Object snapshot = response.get("snapshot");
        if (snapshot instanceof Map<?, ?> map) {
            block.setSnapshot(new LinkedHashMap<>((Map<String, Object>) map));
        }
        Object output = response.get("output");
        if (output instanceof Map<?, ?> map) {
            block.setOutput(new LinkedHashMap<>((Map<String, Object>) map));
        }
        Object error = response.get("error");
        if (error instanceof Map<?, ?> map) {
            block.setError(new LinkedHashMap<>((Map<String, Object>) map));
        }
        String status = stringValue(response.get("status"));
        if (status != null && !status.isBlank()) {
            block.setStatus(status);
        }
    }

    private void finalizeSyncBlock(ManagedObject object, AutomationBlockExecution block, Map<String, Object> response) {
        String status = stringValue(response.getOrDefault("status", "COMPLETED"));
        block.setStatus(status);
        block.setFinishedAt(Instant.now());
        block.setUpdatedAt(Instant.now());
        CallApiActionSupport.applyResponseMappings(object, response, block.getOutputMappings());
        Object output = response.get("output");
        if (output instanceof Map<?, ?> outputMap) {
            CallApiActionSupport.applyResponseMappings(object, new LinkedHashMap<>((Map<String, Object>) outputMap), block.getOutputMappings());
        }
        if (block.getStoreOutputAt() != null && !block.getStoreOutputAt().isBlank()) {
            ActionPayloadSupport.setPayloadPath(object, block.getStoreOutputAt(), response);
        }
        applyExecutionStores(object, block, response);
        if ("FAILED".equalsIgnoreCase(status) || "TIMED_OUT".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) {
            if (block.getFailurePolicy() == AutomationFailurePolicy.FAIL_FAST) {
                throw new IllegalStateException("automation block failed: " + block.getBlockKey());
            }
        }
    }

    private void applyExecutionStores(ManagedObject object, AutomationBlockExecution block, Map<String, Object> response) {
        applyOptionalStore(object, block.getStoreExecutionIdAt(), firstNonBlank(stringValue(response.get("executionId")), stringValue(response.get("id"))));
        applyOptionalStore(object, block.getStoreStatusAt(), response.get("status"));
        Object output = response.get("output");
        applyOptionalStore(object, block.getStoreVariablesAt(), output instanceof Map<?, ?> ? output : response.get("variables"));
        applyOptionalStore(object, block.getStoreFullExecutionAt(), response);
        Object error = response.get("error");
        if (error != null) {
            applyOptionalStore(object, block.getStoreErrorAt(), error);
        } else {
            removeOptionalStore(object, block.getStoreErrorAt());
        }
    }

    private void applyOptionalStore(ManagedObject object, String path, Object value) {
        if (path == null || path.isBlank()) {
            return;
        }
        ActionPayloadSupport.setPayloadPath(object, path, value);
    }

    private void removeOptionalStore(ManagedObject object, String path) {
        if (path == null || path.isBlank()) {
            return;
        }
        ActionPayloadSupport.removePayloadPath(object, path);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> resolveMapTemplate(Object value, ManagedObject object, String actorUserId) {
        Object resolved = CallApiActionSupport.resolveTemplate(value, object, actorUserId);
        return resolved instanceof Map<?, ?> map ? new LinkedHashMap<>((Map<String, Object>) map) : new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asStringObjectMap(Object value) {
        return value instanceof Map<?, ?> map ? new LinkedHashMap<>((Map<String, Object>) map) : new LinkedHashMap<>();
    }

    private AutomationExecutionMode parseMode(Object value, Object asyncValue) {
        if (asyncValue != null && (value == null || stringValue(value).isBlank())) {
            return Boolean.parseBoolean(String.valueOf(asyncValue)) ? AutomationExecutionMode.ASYNC : AutomationExecutionMode.SYNC;
        }
        String resolved = stringValue(value);
        if (resolved == null || resolved.isBlank()) {
            return AutomationExecutionMode.ASYNC;
        }
        return AutomationExecutionMode.valueOf(resolved.trim().toUpperCase());
    }

    private ConditionOperator parseConditionOperator(Object value) {
        String resolved = stringValue(value);
        if (resolved == null || resolved.isBlank()) {
            return ConditionOperator.EQ;
        }
        return ConditionOperator.valueOf(resolved.trim().toUpperCase());
    }

    private AutomationFailurePolicy parseFailurePolicy(Object value) {
        String resolved = stringValue(value);
        if (resolved == null || resolved.isBlank()) {
            return AutomationFailurePolicy.MARK_FAILED;
        }
        return AutomationFailurePolicy.valueOf(resolved.trim().toUpperCase());
    }

    @SuppressWarnings("unchecked")
    private void updateFields(Map<String, Object> payload, Map<String, Object> params) {
        Object fields = params.get("fields");
        if (fields instanceof Map<?, ?> map) {
            map.forEach((key, value) -> payload.put(String.valueOf(key), value));
        }
    }

    private void copyFields(Map<String, Object> payload, Map<String, Object> params) {
        String from = stringValue(params.get("from"));
        String to = stringValue(params.get("to"));
        if (from != null && to != null && payload.containsKey(from)) {
            payload.put(to, payload.get(from));
        }
    }

    private void removeFields(Map<String, Object> payload, Map<String, Object> params) {
        Object fields = params.get("fields");
        if (fields instanceof List<?> list) {
            for (Object field : list) {
                payload.remove(String.valueOf(field));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Set<String> stringSet(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).collect(java.util.stream.Collectors.toSet());
        }
        return Set.of();
    }

    private Integer intValue(Object value, int fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException ignored) {
            }
        }
        return fallback;
    }

    private Long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value != null) {
            try {
                return Long.parseLong(String.valueOf(value));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Object firstPresent(Map<String, Object> params, String... keys) {
        for (String key : keys) {
            if (params.containsKey(key) && params.get(key) != null) {
                return params.get(key);
            }
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
