package com.cyancoder.bpm.service;

import com.cyancoder.bpm.api.dto.MetadataFieldDescriptor;
import com.cyancoder.bpm.api.dto.StateActionStructureResponse;
import com.cyancoder.bpm.api.dto.TransitionConditionOperatorDescriptor;
import com.cyancoder.bpm.api.dto.TransitionConditionStructureResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DynamicFlowMetadataService {

    public List<StateActionStructureResponse> stateActionStructures() {
        List<MetadataFieldDescriptor> commonFields = List.of(
                new MetadataFieldDescriptor("whenExpression", "string", false, "Optional expression gate evaluated before the action runs.", "#payload.currentFormValues.verified == true"),
                new MetadataFieldDescriptor("whenField", "string", false, "Optional field-path gate used with whenOperator/whenValue.", "payload.currentFormValues.age"),
                new MetadataFieldDescriptor("whenOperator", "string", false, "Operator for whenField comparison.", "GT"),
                new MetadataFieldDescriptor("whenValue", "any", false, "Comparison value for whenField.", 18),
                new MetadataFieldDescriptor("continueOnError", "boolean", false, "If true, execution continues when this action fails.", true)
        );

        return List.of(
                new StateActionStructureResponse("NONE", List.of(), "No-op action.", commonFields, List.of()),
                new StateActionStructureResponse("ADD_AUDIT_ENTRY", List.of(), "Appends a message to the managed object audit log.", commonFields, List.of(
                        new MetadataFieldDescriptor("message", "string", false, "Audit log message.", "Entered review state")
                )),
                new StateActionStructureResponse("NOTIFY_OWNER", List.of(), "Calls notification integration with an arbitrary payload.", commonFields, List.of(
                        new MetadataFieldDescriptor("serviceKey", "string", false, "Target service key.", "notification-service"),
                        new MetadataFieldDescriptor("path", "string", false, "Target internal path.", "/internal/notifications/send"),
                        new MetadataFieldDescriptor("method", "string", false, "HTTP method. Defaults to POST.", "POST"),
                        new MetadataFieldDescriptor("body", "object|array|string", false, "Templated request body.", Map.of("templateKey", "review-started"))
                )),
                new StateActionStructureResponse("CALL_API", List.of("CALL_OPERATOR"), "Calls an internal or external HTTP API synchronously and can map the response into payload.", commonFields, callApiParams(false)),
                new StateActionStructureResponse("CALL_API_ASYNC", List.of("CALL_OPERATOR_ASYNC"), "Starts an async HTTP/API action and optionally waits for callback mappings.", commonFields, callApiParams(true)),
                new StateActionStructureResponse("LOCK_OBJECT", List.of(), "Locks the managed object against edits.", commonFields, List.of()),
                new StateActionStructureResponse("UNLOCK_OBJECT", List.of(), "Unlocks the managed object.", commonFields, List.of()),
                new StateActionStructureResponse("UPDATE_OBJECT_FIELDS", List.of(), "Writes one or more payload/object paths.", commonFields, List.of(
                        new MetadataFieldDescriptor("fields", "object", false, "Map of field path -> value.", Map.of("payload.currentFormValues.verified", true))
                )),
                new StateActionStructureResponse("COPY_FIELDS", List.of(), "Copies values from one payload path to another.", commonFields, List.of(
                        new MetadataFieldDescriptor("from", "string", false, "Single source path.", "payload.currentFormValues.nationalCode"),
                        new MetadataFieldDescriptor("to", "string", false, "Single target path.", "payload.audit.nationalCode")
                )),
                new StateActionStructureResponse("REMOVE_FIELDS", List.of(), "Removes one or more payload paths.", commonFields, List.of(
                        new MetadataFieldDescriptor("fields", "array<string>", false, "List of field paths to remove.", List.of("payload.temp", "payload.operatorResults.rawResponse"))
                )),
                new StateActionStructureResponse("SET_ASSIGNEE", List.of(), "Changes the object assignee.", commonFields, List.of(
                        new MetadataFieldDescriptor("assignee", "string", false, "Direct assignee user id.", "admin-user-1")
                )),
                new StateActionStructureResponse("SET_ACCESS_RULE", List.of(), "Applies a new access rule to the current object.", commonFields, List.of(
                        new MetadataFieldDescriptor("canRead", "array<string>", false, "Groups/roles allowed to read.", List.of("creator", "admin")),
                        new MetadataFieldDescriptor("canEdit", "array<string>", false, "Groups/roles allowed to edit.", List.of("admin")),
                        new MetadataFieldDescriptor("canApprove", "array<string>", false, "Groups/roles allowed to approve.", List.of("admin"))
                )),
                new StateActionStructureResponse("RUN_AUTOMATION_BLOCK", List.of(), "Runs automation from BPM, synchronously or as a waiting async block.", commonFields, List.of(
                        new MetadataFieldDescriptor("blockKey", "string", false, "Stable block key for the managed object registry.", "screening"),
                        new MetadataFieldDescriptor("automationFlowKey", "string", false, "Active automation flow key to run.", "hybrid-screening-automation"),
                        new MetadataFieldDescriptor("executionMode", "string", false, "SYNC or ASYNC.", "ASYNC"),
                        new MetadataFieldDescriptor("failurePolicy", "string", false, "FAIL_FAST, MARK_FAILED, or RETRY.", "RETRY"),
                        new MetadataFieldDescriptor("body", "object|array|string", false, "Templated block input.", Map.of("customerId", "{{payload.currentFormValues.customerId}}")),
                        new MetadataFieldDescriptor("inlineFragment", "object", false, "Optional inline automation fragment snapshot.", Map.of("entryNodeId", "trigger")),
                        new MetadataFieldDescriptor("responseMappings", "object", false, "Map of payload path -> response path for the start response.", Map.of("payload.automation.screening.executionId", "executionId")),
                        new MetadataFieldDescriptor("storeFullResponseAt", "string", false, "Payload path to store the full start response.", "payload.automation.screening.startResponse"),
                        new MetadataFieldDescriptor("callbackResponseMappings", "object", false, "Map of payload path -> callback payload path.", Map.of("payload.currentFormValues.riskScore", "riskScore")),
                        new MetadataFieldDescriptor("callbackStoreFullResponseAt", "string", false, "Payload path to store the full callback payload.", "payload.automation.screening.snapshot"),
                        new MetadataFieldDescriptor("nextStateOnSuccess", "string", false, "Optional state to route to after a successful callback.", "fast-track-approved"),
                        new MetadataFieldDescriptor("nextStateOnFailure", "string", false, "Optional state to route to after a failed callback.", "manual-review"),
                        new MetadataFieldDescriptor("maxRetries", "integer", false, "Retry attempts for RETRY failure policy.", 1),
                        new MetadataFieldDescriptor("timeoutSeconds", "integer", false, "Async timeout in seconds.", 300)
                ))
        );
    }

    public TransitionConditionStructureResponse transitionConditionStructure() {
        return new TransitionConditionStructureResponse(
                List.of(
                        new MetadataFieldDescriptor("field", "string", true, "Payload/context/object path used in a single condition.", "payload.currentFormValues.age"),
                        new MetadataFieldDescriptor("operator", "string", true, "Comparison operator.", "GT"),
                        new MetadataFieldDescriptor("value", "any", false, "Comparison value. Shape depends on operator.", 18)
                ),
                List.of(
                        new TransitionConditionOperatorDescriptor("EQ", "scalar", "Equals.", true),
                        new TransitionConditionOperatorDescriptor("NE", "scalar", "Not equals.", false),
                        new TransitionConditionOperatorDescriptor("GT", "scalar-number", "Greater than.", 18),
                        new TransitionConditionOperatorDescriptor("GTE", "scalar-number", "Greater than or equal.", 18),
                        new TransitionConditionOperatorDescriptor("LT", "scalar-number", "Less than.", 18),
                        new TransitionConditionOperatorDescriptor("LTE", "scalar-number", "Less than or equal.", 18),
                        new TransitionConditionOperatorDescriptor("BEFORE", "scalar-date", "Date is before value.", "2025-01-01"),
                        new TransitionConditionOperatorDescriptor("AFTER", "scalar-date", "Date is after value.", "2025-01-01"),
                        new TransitionConditionOperatorDescriptor("BETWEEN", "array[2]", "Value is between lower and upper bounds.", List.of(18, 65)),
                        new TransitionConditionOperatorDescriptor("NOT_BETWEEN", "array[2]", "Value is outside lower and upper bounds.", List.of(18, 65)),
                        new TransitionConditionOperatorDescriptor("IN", "array", "Field value is one of provided values.", List.of("IT", "INFRA")),
                        new TransitionConditionOperatorDescriptor("NOT_IN", "array", "Field value is not one of provided values.", List.of("IT", "INFRA")),
                        new TransitionConditionOperatorDescriptor("EXISTS", "null", "Field exists.", null),
                        new TransitionConditionOperatorDescriptor("IS_NULL", "null", "Field is null.", null),
                        new TransitionConditionOperatorDescriptor("NOT_NULL", "null", "Field is not null.", null),
                        new TransitionConditionOperatorDescriptor("EMPTY", "null", "Field is empty.", null),
                        new TransitionConditionOperatorDescriptor("NOT_EMPTY", "null", "Field is not empty.", null),
                        new TransitionConditionOperatorDescriptor("CONTAINS", "scalar", "Array/string contains value.", "admin"),
                        new TransitionConditionOperatorDescriptor("CONTAINS_ANY", "array", "Array contains any provided value.", List.of("admin", "reviewer")),
                        new TransitionConditionOperatorDescriptor("STARTS_WITH", "scalar-string", "String starts with value.", "09"),
                        new TransitionConditionOperatorDescriptor("ENDS_WITH", "scalar-string", "String ends with value.", ".pdf"),
                        new TransitionConditionOperatorDescriptor("MATCHES", "regex-string", "String matches regex pattern.", "^[0-9]{10}$")
                ),
                List.of("AND", "OR"),
                true,
                List.of("payload.*", "context.*", "actorUserId", "actorGroups", "actorRoles", "currentState", "objectType", "flowKey")
        );
    }

    private List<MetadataFieldDescriptor> callApiParams(boolean async) {
        List<MetadataFieldDescriptor> params = new ArrayList<>(List.of(
                new MetadataFieldDescriptor("serviceKey", "string", false, "Target service key for internal calls.", "processor-service"),
                new MetadataFieldDescriptor("path", "string", false, "Target path. Template expressions are supported.", "/internal/processor/forms/submit"),
                new MetadataFieldDescriptor("url", "string", false, "Direct URL for external calls.", "https://example.test/verify"),
                new MetadataFieldDescriptor("method", "string", false, "HTTP method. Defaults to POST.", "POST"),
                new MetadataFieldDescriptor("headers", "object", false, "Map of HTTP header -> value.", Map.of("X-API-KEY", "secret")),
                new MetadataFieldDescriptor("body", "object|array|string", false, "Templated request body.", Map.of("nationalCode", "{{payload.currentFormValues.nationalCode}}")),
                new MetadataFieldDescriptor("responseMappings", "object", false, "Map of payload path -> response path.", Map.of("payload.currentFormValues.verified", "verified")),
                new MetadataFieldDescriptor("storeFullResponseAt", "string", false, "Stores the full response at a payload path.", "payload.operatorResults.verify")
        ));
        if (async) {
            params.addAll(List.of(
                    new MetadataFieldDescriptor("actionKey", "string", false, "Stable async action key.", "screening"),
                    new MetadataFieldDescriptor("correlationKey", "string", false, "Optional callback correlation key.", "{{payload.currentFormValues.nationalCode}}:screening"),
                    new MetadataFieldDescriptor("callbackResponseMappings", "object", false, "Map of payload path -> callback payload path.", Map.of("payload.currentFormValues.riskScore", "riskScore")),
                    new MetadataFieldDescriptor("callbackStoreFullResponseAt", "string", false, "Stores the full callback payload at a payload path.", "payload.operatorResults.screeningCallback")
            ));
        }
        return params;
    }
}
