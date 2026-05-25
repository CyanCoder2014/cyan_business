package com.cyancoder.bpm.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DynamicFlowMetadataService {

    public List<Map<String, Object>> stateActionStructures() {
        return List.of(
                Map.of("type", "NONE", "description", "No-op action."),
                Map.of("type", "ADD_AUDIT_ENTRY", "description", "Appends a message to the managed object audit log.", "params", List.of("message")),
                Map.of("type", "NOTIFY_OWNER", "description", "Calls notification integration.", "params", List.of("serviceKey", "path", "method")),
                Map.of("type", "CALL_API", "description", "Calls external or internal HTTP API synchronously.", "params", List.of("serviceKey", "path", "method")),
                Map.of("type", "RUN_AUTOMATION_BLOCK", "description", "Runs automation as a first-class BPM block with sync/async mode, failure policy, mappings, retry, timeout, and callback semantics.", "params", List.of("blockKey", "automationFlowKey", "executionMode", "failurePolicy", "body", "inlineFragment", "responseMappings", "storeFullResponseAt", "callbackResponseMappings", "callbackStoreFullResponseAt", "maxRetries", "timeoutSeconds", "nextStateOnSuccess", "nextStateOnFailure")),
                Map.of("type", "CALL_OPERATOR", "description", "Calls operator-style API.", "params", List.of("serviceKey", "path", "method")),
                Map.of("type", "UPDATE_OBJECT_FIELDS", "description", "Writes payload/object fields.", "params", List.of("fields")),
                Map.of("type", "COPY_FIELDS", "description", "Copies one payload field to another.", "params", List.of("from", "to")),
                Map.of("type", "REMOVE_FIELDS", "description", "Removes one or more payload fields.", "params", List.of("fields")),
                Map.of("type", "SET_ASSIGNEE", "description", "Sets assignee.", "params", List.of("assignee")),
                Map.of("type", "SET_ACCESS_RULE", "description", "Sets access rule.", "params", List.of("canRead", "canEdit", "canApprove"))
        );
    }

    public Map<String, Object> transitionConditionStructure() {
        return Map.of(
                "operators", List.of("EQ", "NE", "GT", "GTE", "LT", "LTE", "IN", "NOT_IN", "EXISTS", "IS_NULL", "NOT_NULL", "EMPTY", "NOT_EMPTY", "CONTAINS", "STARTS_WITH", "ENDS_WITH"),
                "logicalOperators", List.of("AND", "OR"),
                "supportedFields", List.of("payload.*", "context.*", "actorUserId", "actorGroups", "actorRoles", "currentState", "objectType", "flowKey")
        );
    }
}
