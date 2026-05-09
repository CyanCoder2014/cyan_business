package com.cyancoder.bpm.service;

import com.cyancoder.bpm.api.dto.BpmScope;
import com.cyancoder.bpm.api.dto.FormSubmissionSyncRequest;
import com.cyancoder.bpm.api.dto.FormSubmissionSyncResponse;
import com.cyancoder.bpm.domain.FlowActionConfig;
import com.cyancoder.bpm.domain.FlowState;
import com.cyancoder.bpm.domain.ManagedObject;
import com.cyancoder.bpm.domain.SubmitMode;
import com.cyancoder.dynamiccore.runtime.DynamicRecordRequest;
import com.cyancoder.dynamiccore.store.jpa.StoredEntityDefinition;
import com.cyancoder.dynamiccore.store.mongo.DynamicEntityRecordDocument;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DynamicFlowIntegrationClient {
    private final InternalServiceHttpSupport httpSupport;

    public DynamicFlowIntegrationClient(InternalServiceHttpSupport httpSupport) {
        this.httpSupport = httpSupport;
    }

    public Map<String, Object> fetchRendererDefinition(FlowState state, BpmScope scope) {
        String serviceKey = coalesce(state.rendererService(), state.entityService());
        String entityKey = coalesce(state.rendererKey(), state.formKey(), state.entityKey());
        if (serviceKey == null || entityKey == null) {
            return null;
        }
        StoredEntityDefinition definition = httpSupport.get(
                serviceKey,
                "/internal/entities/definitions/" + entityKey,
                scope.tenantKey(),
                scope.siteKey(),
                StoredEntityDefinition.class
        );
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("serviceKey", definition.getServiceKey());
        result.put("entityKey", definition.getEntityKey());
        result.put("entityType", definition.getEntityType());
        result.put("title", definition.getTitle());
        result.put("definitionJson", definition.getDefinitionJson());
        return result;
    }

    public FormSubmissionSyncResponse submitForm(FlowState state, FormSubmissionSyncRequest request, BpmScope scope) {
        SubmitMode mode = state.submitMode() == null ? SubmitMode.DYNAMIC : state.submitMode();
        if (mode == SubmitMode.STATIC) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("objectId", request.objectId());
            body.put("objectType", request.objectType());
            body.put("flowKey", request.flowKey());
            body.put("stateId", request.stateId());
            body.put("formData", request.formData());
            body.put("objectPayload", request.objectPayload());
            body.put("context", request.context());
            @SuppressWarnings("unchecked")
            Map<String, Object> response = httpSupport.exchange(
                    state.entityService(),
                    state.submitUrl(),
                    HttpMethod.POST,
                    body,
                    scope.tenantKey(),
                    scope.siteKey(),
                    Map.class
            );
            String submittedId = response == null ? null : stringValue(response.get("submittedFormId"));
            Map<String, Object> values = response == null ? Map.of() : asMap(response.get("currentFormValues"));
            return new FormSubmissionSyncResponse(true, "submitted", submittedId, values);
        }

        DynamicRecordRequest submitRequest = new DynamicRecordRequest();
        submitRequest.setRecordKey(request.existingSubmissionId());
        submitRequest.setTenantKey(scope.tenantKey());
        submitRequest.setSiteKey(scope.siteKey());
        submitRequest.setData(request.formData());
        DynamicEntityRecordDocument document = httpSupport.post(
                state.entityService(),
                "/internal/entities/records/" + state.entityKey(),
                submitRequest,
                scope.tenantKey(),
                scope.siteKey(),
                DynamicEntityRecordDocument.class
        );
        return new FormSubmissionSyncResponse(true, "submitted", document.getRecordKey(), document.getData());
    }

    public void callAction(FlowActionConfig action, ManagedObject object, BpmScope scope) {
        if (action.params() == null) {
            return;
        }
        String serviceKey = stringValue(action.params().get("serviceKey"));
        String path = stringValue(action.params().get("path"));
        if (serviceKey == null || path == null) {
            return;
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("managedObjectId", object.getId());
        body.put("payload", object.getPayload());
        body.put("objectRef", object.getObjectRef());
        body.put("params", action.params());
        httpSupport.exchange(
                serviceKey,
                path,
                HttpMethod.valueOf(stringValue(action.params().getOrDefault("method", "POST"))),
                body,
                scope.tenantKey(),
                scope.siteKey(),
                Map.class
        );
    }

    public Map<String, Object> callActionForResponse(FlowActionConfig action, ManagedObject object, BpmScope scope, String actor) {
        if (action.params() == null) {
            return Map.of();
        }
        String serviceKey = stringValue(action.params().get("serviceKey"));
        String path = stringValue(action.params().get("path"));
        if (action.type() == com.cyancoder.bpm.domain.ActionType.START_AUTOMATION_FLOW) {
            serviceKey = coalesce(serviceKey, "automation-orchestrator-service");
            path = coalesce(path, "/internal/automation-orchestrator/executions/start");
        }
        if (serviceKey == null || path == null) {
            return Map.of();
        }
        Object body = CallApiActionSupport.resolveTemplate(action.params().get("body"), object, actor);
        @SuppressWarnings("unchecked")
        Map<String, Object> response = httpSupport.exchange(
                serviceKey,
                path,
                HttpMethod.valueOf(stringValue(action.params().getOrDefault("method", "POST"))),
                body == null ? Map.of() : body,
                scope.tenantKey(),
                scope.siteKey(),
                Map.class
        );
        return response == null ? Map.of() : response;
    }

    private String coalesce(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
