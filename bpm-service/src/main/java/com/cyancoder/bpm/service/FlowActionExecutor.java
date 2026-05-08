package com.cyancoder.bpm.service;

import com.cyancoder.bpm.api.dto.BpmScope;
import com.cyancoder.bpm.domain.ActionType;
import com.cyancoder.bpm.domain.FlowActionConfig;
import com.cyancoder.bpm.domain.FlowAccessRule;
import com.cyancoder.bpm.domain.ManagedObject;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class FlowActionExecutor {
    private final DynamicFlowIntegrationClient integrationClient;

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
            apply(action, object, scope, actorUserId);
        }
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
        com.cyancoder.bpm.domain.AsyncActionRegistration registration = new com.cyancoder.bpm.domain.AsyncActionRegistration();
        registration.setActionKey(actionKey);
        registration.setCorrelationKey(correlationKey);
        registration.setStateId(object.getState());
        registration.setStatus("PENDING");
        object.getAsyncActionRegistry().removeIf(existing -> actionKey.equals(existing.getActionKey()));
        object.getAsyncActionRegistry().add(registration);
        ActionPayloadSupport.setPayloadPath(object, "payload.asyncActions." + actionKey + ".status", "PENDING");
        ActionPayloadSupport.setPayloadPath(object, "payload.asyncActions." + actionKey + ".correlationKey", correlationKey);
        if (params.get("callbackResponseMappings") != null) {
            ActionPayloadSupport.setPayloadPath(object, "payload.asyncActions." + actionKey + ".callbackResponseMappings", params.get("callbackResponseMappings"));
        }
        if (params.get("callbackStoreFullResponseAt") != null) {
            ActionPayloadSupport.setPayloadPath(object, "payload.asyncActions." + actionKey + ".callbackStoreFullResponseAt", params.get("callbackStoreFullResponseAt"));
        }
        if (!Boolean.FALSE.equals(params.get("fireAndForget"))) {
            integrationClient.callActionForResponse(action, object, scope, actorUserId);
        }
    }

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

    @SuppressWarnings("unchecked")
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

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
