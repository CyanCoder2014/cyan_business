package com.cyancoder.bpm.service;

import com.cyancoder.bpm.api.dto.BpmScope;
import com.cyancoder.bpm.api.dto.CreateManagedObjectRequest;
import com.cyancoder.bpm.api.dto.AsyncActionCallbackRequest;
import com.cyancoder.bpm.api.dto.FormSubmissionSyncRequest;
import com.cyancoder.bpm.api.dto.FormSubmissionSyncResponse;
import com.cyancoder.bpm.api.dto.ManagedObjectActiveFormResponse;
import com.cyancoder.bpm.api.dto.ManagedObjectFormSubmissionResponse;
import com.cyancoder.bpm.api.dto.SubmitManagedObjectFormRequest;
import com.cyancoder.bpm.api.dto.TransitionActorContext;
import com.cyancoder.bpm.api.dto.TransitionOptionResponse;
import com.cyancoder.bpm.domain.DynamicFlowDefinition;
import com.cyancoder.bpm.domain.FlowState;
import com.cyancoder.bpm.domain.FlowTransition;
import com.cyancoder.bpm.domain.ManagedObject;
import com.cyancoder.bpm.domain.TransitionHistoryEntry;
import com.cyancoder.bpm.repo.ManagedObjectRepository;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ObjectFlowService {
    private static final String FORMS_KEY = "_bpmForms";

    private final ManagedObjectRepository managedObjectRepository;
    private final FlowDefinitionService flowDefinitionService;
    private final FlowTransitionConditionEvaluator transitionConditionEvaluator;
    private final DynamicFlowIntegrationClient integrationClient;
    private final FlowActionExecutor actionExecutor;
    private final RuntimeService runtimeService;
    private final TaskService taskService;

    public ObjectFlowService(ManagedObjectRepository managedObjectRepository,
                             FlowDefinitionService flowDefinitionService,
                             FlowTransitionConditionEvaluator transitionConditionEvaluator,
                             DynamicFlowIntegrationClient integrationClient,
                             FlowActionExecutor actionExecutor,
                             ObjectProvider<RuntimeService> runtimeServiceProvider,
                             ObjectProvider<TaskService> taskServiceProvider) {
        this.managedObjectRepository = managedObjectRepository;
        this.flowDefinitionService = flowDefinitionService;
        this.transitionConditionEvaluator = transitionConditionEvaluator;
        this.integrationClient = integrationClient;
        this.actionExecutor = actionExecutor;
        this.runtimeService = runtimeServiceProvider.getIfAvailable();
        this.taskService = taskServiceProvider.getIfAvailable();
    }

    public ManagedObject createAndStart(BpmScope scope, CreateManagedObjectRequest request, TransitionActorContext actorContext) {
        DynamicFlowDefinition definition = flowDefinitionService.getActiveByFlowKey(scope, request.flowKey());
        FlowState startState = findState(definition, definition.getStartState());

        ManagedObject object = new ManagedObject();
        object.setTenantKey(scope.tenantKey());
        object.setSiteKey(scope.siteKey());
        object.setObjectType(request.objectType());
        object.setObjectRef(request.objectRef());
        object.setFlowKey(request.flowKey());
        object.setState(startState.id());
        object.setPayload(request.payload() == null ? new HashMap<>() : new HashMap<>(request.payload()));
        object.setCreatedAt(Instant.now());
        object.setUpdatedAt(Instant.now());

        syncStatePayload(object, startState);
        applyStateEffects(scope, object, startState, actor(actorContext));
        ManagedObject saved = managedObjectRepository.save(object);
        if (runtimeService != null) {
            ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                    .processDefinitionKey(definition.getFlowKey())
                    .businessKey(saved.getId())
                    .variable("managedObjectId", saved.getId())
                    .variable("currentState", definition.getStartState())
                    .start();
            saved.setProcessInstanceId(processInstance.getProcessInstanceId());
            saved = managedObjectRepository.save(saved);
        }
        advanceThroughAutomaticStates(scope, saved, definition, actorContext, Map.of(), actor(actorContext));
        saved.setUpdatedAt(Instant.now());
        saved = managedObjectRepository.save(saved);
        return saved;
    }

    public ManagedObject transit(BpmScope scope, String objectId, String nextState, TransitionActorContext actorContext, Map<String, Object> context) {
        ManagedObject object = findById(scope, objectId);
        DynamicFlowDefinition definition = flowDefinitionService.getActiveByFlowKey(scope, object.getFlowKey());
        FlowTransition transition = resolveTransition(definition, object, nextState, actorContext, context == null ? Map.of() : context);
        String actor = actor(actorContext);
        String previousState = object.getState();

        if (taskService != null) {
            Task activeTask = taskService.createTaskQuery()
                    .processInstanceBusinessKey(objectId)
                    .active()
                    .singleResult();
            if (activeTask == null) {
                throw new IllegalArgumentException("active task not found");
            }
            taskService.complete(activeTask.getId(), Map.of(
                    "nextState", transition.toState(),
                    "currentState", transition.toState()
            ));
        }

        appendTransitionHistory(object, transition, previousState, transition.toState(), actor, context);
        object.setState(transition.toState());
        FlowState newState = findState(definition, transition.toState());
        syncStatePayload(object, newState);
        applyStateEffects(scope, object, newState, actor);
        advanceThroughAutomaticStates(scope, object, definition, actorContext, context == null ? Map.of() : context, actor);
        object.setUpdatedAt(Instant.now());
        return managedObjectRepository.save(object);
    }

    public ManagedObjectActiveFormResponse getActiveForm(BpmScope scope, String objectId) {
        ManagedObject object = findById(scope, objectId);
        DynamicFlowDefinition definition = flowDefinitionService.getActiveByFlowKey(scope, object.getFlowKey());
        FlowState state = findState(definition, object.getState());
        Map<String, Object> renderer = integrationClient.fetchRendererDefinition(state, scope);
        return new ManagedObjectActiveFormResponse(
                object.getId(),
                object.getObjectType(),
                object.getFlowKey(),
                object.getState(),
                state.formKey(),
                state.processorKey(),
                readStateSubmissionId(object, state.id()),
                state.accessRule(),
                renderer,
                state.entityService(),
                state.entityKey(),
                String.valueOf(state.submitMode() == null ? com.cyancoder.bpm.domain.SubmitMode.DYNAMIC : state.submitMode())
        );
    }

    public ManagedObjectFormSubmissionResponse submitActiveForm(BpmScope scope, String objectId, SubmitManagedObjectFormRequest request, TransitionActorContext actorContext) {
        ManagedObject object = findById(scope, objectId);
        DynamicFlowDefinition definition = flowDefinitionService.getActiveByFlowKey(scope, object.getFlowKey());
        FlowState state = findState(definition, object.getState());

        FormSubmissionSyncResponse submission = integrationClient.submitForm(state, new FormSubmissionSyncRequest(
                object.getId(),
                object.getObjectType(),
                object.getFlowKey(),
                state.id(),
                state.formKey(),
                state.processorKey(),
                readStateSubmissionId(object, state.id()),
                actor(actorContext),
                request.formData() == null ? Map.of() : request.formData(),
                object.getPayload(),
                request.context() == null ? Map.of() : request.context()
        ), scope);

        applySubmission(object, state, actor(actorContext), submission);
        managedObjectRepository.save(object);

        ManagedObject result = request.nextState() == null || request.nextState().isBlank()
                ? object
                : transit(scope, objectId, request.nextState(), actorContext, request.context());
        return new ManagedObjectFormSubmissionResponse(result, submission.submittedFormId(), submission.currentFormValues());
    }

    public ManagedObject findById(BpmScope scope, String objectId) {
        ManagedObject object = managedObjectRepository.findById(objectId).orElseThrow();
        if (!equalsNullable(scope.tenantKey(), object.getTenantKey()) || !equalsNullable(scope.siteKey(), object.getSiteKey())) {
            throw new IllegalArgumentException("managed object not found");
        }
        return object;
    }

    public List<ManagedObject> findAll(BpmScope scope) {
        return managedObjectRepository.findByTenantKeyAndSiteKeyOrderByUpdatedAtDesc(scope.tenantKey(), scope.siteKey());
    }

    public List<ManagedObject> findAllByAssignee(BpmScope scope, String assignee) {
        return managedObjectRepository.findByTenantKeyAndSiteKeyAndAssigneeOrderByUpdatedAtDesc(scope.tenantKey(), scope.siteKey(), assignee);
    }

    public List<ManagedObject> findAllVisibleToActor(BpmScope scope, TransitionActorContext actorContext) {
        Set<String> roles = actorContext == null || actorContext.roles() == null ? Set.of() : actorContext.roles();
        List<ManagedObject> visible = new ArrayList<>();
        for (ManagedObject object : findAll(scope)) {
            if (object.getAccessRule() == null || object.getAccessRule().canRead() == null || object.getAccessRule().canRead().isEmpty()) {
                visible.add(object);
                continue;
            }
            for (String candidate : object.getAccessRule().canRead()) {
                if (roles.contains(candidate) || candidate.equals(actor(actorContext))) {
                    visible.add(object);
                    break;
                }
            }
        }
        return visible;
    }

    public List<TransitionOptionResponse> availableTransitions(BpmScope scope, String objectId, TransitionActorContext actorContext, Map<String, Object> context) {
        ManagedObject object = findById(scope, objectId);
        DynamicFlowDefinition definition = flowDefinitionService.getActiveByFlowKey(scope, object.getFlowKey());
        if (taskService != null) {
            Task activeTask = taskService.createTaskQuery()
                    .processInstanceBusinessKey(objectId)
                    .active()
                    .singleResult();
            if (activeTask == null) {
                return List.of();
            }
        }
        List<TransitionOptionResponse> options = new ArrayList<>();
        for (FlowTransition transition : definition.getTransitions()) {
            if (!object.getState().equals(transition.fromState())) {
                continue;
            }
            if (!allowed(transition, actorContext)) {
                continue;
            }
            if (!transitionConditionEvaluator.evaluate(transition, object.getPayload(), context == null ? Map.of() : context, object, actorContext)) {
                continue;
            }
            options.add(new TransitionOptionResponse(transition.id(), transition.label(), transition.fromState(), transition.toState()));
        }
        return options;
    }

    public ManagedObject acceptAsyncActionCallback(BpmScope scope,
                                                   String objectId,
                                                   String actionKey,
                                                   AsyncActionCallbackRequest request,
                                                   TransitionActorContext actorContext,
                                                   String callbackFingerprint) {
        ManagedObject object = findById(scope, objectId);
        Map<String, Object> asyncEntry = asyncActionEntry(object, actionKey);
        if (isDuplicateCallback(asyncEntry, callbackFingerprint)) {
            object.getAuditLog().add("duplicate async callback " + actionKey + " ignored at=" + Instant.now());
            return object;
        }
        Object callbackMappings = asyncEntry.get("callbackResponseMappings");
        Object callbackStoreFullResponseAt = asyncEntry.get("callbackStoreFullResponseAt");
        Map<String, Object> callbackPayload = request == null || request.payload() == null ? Map.of() : request.payload();
        CallApiActionSupport.applyResponseMappingsFromCallback(object, callbackPayload, callbackMappings);
        if (callbackStoreFullResponseAt != null) {
            ActionPayloadSupport.setPayloadPath(object, callbackStoreFullResponseAt.toString(), callbackPayload);
        }
        rememberProcessedCallback(object, actionKey, callbackFingerprint);
        ActionPayloadSupport.setPayloadPath(object, "payload.asyncActions." + actionKey + ".status", "SUCCESS");
        ActionPayloadSupport.setPayloadPath(object, "payload.asyncActions." + actionKey + ".finishedAt", Instant.now().toString());
        ActionPayloadSupport.removePayloadPath(object, "payload.asyncActions." + actionKey + ".error");
        markAsyncRegistrationStatus(object, actionKey, "SUCCESS");
        object.getAuditLog().add("async callback " + actionKey + " received at=" + Instant.now());
        object.setUpdatedAt(Instant.now());
        managedObjectRepository.save(object);

        DynamicFlowDefinition definition = flowDefinitionService.getActiveByFlowKey(scope, object.getFlowKey());
        if (isAutomaticState(findState(definition, object.getState())) && !hasPendingAsyncForCurrentState(object)) {
            return transit(scope,
                    objectId,
                    request == null ? null : request.nextState(),
                    actorContext == null ? new TransitionActorContext("system-callback", Set.of(), Set.of()) : actorContext,
                    request == null || request.context() == null ? Map.of() : request.context());
        }
        return object;
    }

    public ManagedObject acceptAsyncActionCallbackByCorrelationKey(BpmScope scope,
                                                                   String correlationKey,
                                                                   AsyncActionCallbackRequest request,
                                                                   TransitionActorContext actorContext,
                                                                   String callbackFingerprint) {
        ManagedObject object = managedObjectRepository.findFirstByTenantKeyAndSiteKeyAndAsyncActionRegistryCorrelationKey(scope.tenantKey(), scope.siteKey(), correlationKey)
                .orElseThrow();
        String actionKey = resolveActionKeyByCorrelation(object, correlationKey);
        return acceptAsyncActionCallback(scope, object.getId(), actionKey, request, actorContext, callbackFingerprint);
    }

    private FlowTransition resolveTransition(DynamicFlowDefinition definition, ManagedObject object, String nextState, TransitionActorContext actorContext, Map<String, Object> context) {
        FlowTransition resolved = null;
        for (FlowTransition transition : definition.getTransitions()) {
            if (!object.getState().equals(transition.fromState())) {
                continue;
            }
            if (nextState != null && !nextState.isBlank() && !nextState.equals(transition.toState())) {
                continue;
            }
            if (!allowed(transition, actorContext)) {
                continue;
            }
            if (!transitionConditionEvaluator.evaluate(transition, object.getPayload(), context, object, actorContext)) {
                continue;
            }
            resolved = transition;
            break;
        }
        if (resolved == null) {
            throw new IllegalArgumentException("no valid transition");
        }
        return resolved;
    }

    private boolean allowed(FlowTransition transition, TransitionActorContext actorContext) {
        if (actorContext == null) {
            return true;
        }
        boolean groupsOk = transition.allowedGroups() == null || transition.allowedGroups().isEmpty()
                || actorContext.groups() != null && actorContext.groups().stream().anyMatch(transition.allowedGroups()::contains);
        boolean rolesOk = transition.allowedRoles() == null || transition.allowedRoles().isEmpty()
                || actorContext.roles() != null && actorContext.roles().stream().anyMatch(transition.allowedRoles()::contains);
        return groupsOk && rolesOk;
    }

    private FlowState findState(DynamicFlowDefinition definition, String stateId) {
        return definition.getStates().stream().filter(state -> state.id().equals(stateId)).findFirst().orElseThrow();
    }

    private void syncStatePayload(ManagedObject object, FlowState state) {
        object.getPayload().put("formKey", state.formKey());
        object.getPayload().put("processorKey", state.processorKey());
        object.getPayload().put("entityService", state.entityService());
        object.getPayload().put("entityKey", state.entityKey());
        object.setAccessRule(state.accessRule());
    }

    private void applyStateEffects(BpmScope scope, ManagedObject object, FlowState state, String actor) {
        actionExecutor.execute(state.onEnterActions(), object, scope, actor);
    }

    private void advanceThroughAutomaticStates(BpmScope scope,
                                               ManagedObject object,
                                               DynamicFlowDefinition definition,
                                               TransitionActorContext actorContext,
                                               Map<String, Object> context,
                                               String actor) {
        int guard = 0;
        while (isAutomaticState(findState(definition, object.getState())) && !hasPendingAsyncForCurrentState(object)) {
            guard++;
            if (guard > 20) {
                throw new IllegalStateException("Automatic state chaining exceeded 20 steps for object " + object.getId());
            }
            FlowTransition transition = resolveTransition(definition, object, null, actorContext, context);
            String previousState = object.getState();
            if (taskService != null) {
                Task activeTask = taskService.createTaskQuery()
                        .processInstanceBusinessKey(object.getId())
                        .active()
                        .singleResult();
                if (activeTask == null) {
                    throw new IllegalArgumentException("active task not found");
                }
                String nextState = transition.toState();
                taskService.complete(activeTask.getId(), Map.of(
                        "nextState", nextState,
                        "currentState", nextState
                ));
            }
            appendTransitionHistory(object, transition, previousState, transition.toState(), actor, context);
            object.setState(transition.toState());
            FlowState nextState = findState(definition, transition.toState());
            syncStatePayload(object, nextState);
            applyStateEffects(scope, object, nextState, actor);
        }
    }

    @SuppressWarnings("unchecked")
    private void applySubmission(ManagedObject object, FlowState state, String actor, FormSubmissionSyncResponse submission) {
        Map<String, Object> forms = (Map<String, Object>) object.getPayload().computeIfAbsent(FORMS_KEY, ignored -> new LinkedHashMap<String, Object>());
        Map<String, Object> stateForm = new LinkedHashMap<>();
        stateForm.put("formKey", state.formKey());
        stateForm.put("processorKey", state.processorKey());
        stateForm.put("submittedFormId", submission.submittedFormId());
        stateForm.put("submittedAt", Instant.now().toString());
        stateForm.put("submittedBy", actor);
        stateForm.put("currentValues", submission.currentFormValues());
        forms.put(state.id(), stateForm);
        object.getPayload().put(state.id(), submission.currentFormValues());
        object.setUpdatedAt(Instant.now());
    }

    private String readStateSubmissionId(ManagedObject object, String stateId) {
        Object formsValue = object.getPayload().get(FORMS_KEY);
        if (!(formsValue instanceof Map<?, ?> forms)) {
            return null;
        }
        Object stateValue = forms.get(stateId);
        if (!(stateValue instanceof Map<?, ?> stateMap)) {
            return null;
        }
        Object submissionId = stateMap.get("submittedFormId");
        return submissionId == null ? null : String.valueOf(submissionId);
    }

    private void appendTransitionHistory(ManagedObject object, FlowTransition transition, String fromState, String toState, String actor, Map<String, Object> context) {
        TransitionHistoryEntry entry = new TransitionHistoryEntry();
        entry.setTransitionId(transition.id());
        entry.setLabel(transition.label());
        entry.setFromState(fromState);
        entry.setToState(toState);
        entry.setActorUserId(actor);
        entry.setDecision(context == null ? null : stringValue(context.get("decision")));
        entry.setNote(context == null ? null : stringValue(context.get("reviewComment")));
        entry.setTimestamp(Instant.now());
        object.getTransitionHistory().add(entry);
        object.getAuditLog().add("transition " + fromState + " -> " + toState + " by=" + actor);
    }

    private String actor(TransitionActorContext actorContext) {
        if (actorContext == null || actorContext.userId() == null || actorContext.userId().isBlank()) {
            return "system";
        }
        return actorContext.userId();
    }

    private boolean equalsNullable(String left, String right) {
        if (left == null) {
            return right == null;
        }
        return left.equals(right);
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private boolean isAutomaticState(FlowState state) {
        return state != null
                && !state.terminal()
                && (state.formKey() == null || state.formKey().isBlank())
                && (state.entityKey() == null || state.entityKey().isBlank())
                && (state.submitUrl() == null || state.submitUrl().isBlank());
    }

    private boolean hasPendingAsyncForCurrentState(ManagedObject object) {
        if (object.getAsyncActionRegistry() == null) {
            return false;
        }
        for (var registration : object.getAsyncActionRegistry()) {
            if (registration != null
                    && object.getState() != null
                    && object.getState().equals(registration.getStateId())
                    && "PENDING".equalsIgnoreCase(registration.getStatus())) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asyncActionEntry(ManagedObject object, String actionKey) {
        Object root = object.getPayload().get("asyncActions");
        if (!(root instanceof Map<?, ?> rootMap)) {
            throw new IllegalArgumentException("async action not found");
        }
        Object entry = ((Map<String, Object>) rootMap).get(actionKey);
        if (!(entry instanceof Map<?, ?> entryMap)) {
            throw new IllegalArgumentException("async action not found");
        }
        return (Map<String, Object>) entryMap;
    }

    @SuppressWarnings("unchecked")
    private boolean isDuplicateCallback(Map<String, Object> asyncEntry, String callbackFingerprint) {
        if (callbackFingerprint == null || callbackFingerprint.isBlank()) {
            return false;
        }
        Object processed = asyncEntry.get("processedCallbacks");
        if (!(processed instanceof List<?> list)) {
            return false;
        }
        return list.stream().anyMatch(callbackFingerprint::equals);
    }

    @SuppressWarnings("unchecked")
    private void rememberProcessedCallback(ManagedObject object, String actionKey, String callbackFingerprint) {
        if (callbackFingerprint == null || callbackFingerprint.isBlank()) {
            return;
        }
        Object asyncActions = object.getPayload().computeIfAbsent("asyncActions", ignored -> new LinkedHashMap<String, Object>());
        if (!(asyncActions instanceof Map<?, ?> actionsMap)) {
            return;
        }
        Map<String, Object> actionEntry = (Map<String, Object>) ((Map<String, Object>) actionsMap)
                .computeIfAbsent(actionKey, ignored -> new LinkedHashMap<String, Object>());
        Object processed = actionEntry.get("processedCallbacks");
        List<String> callbacks = new ArrayList<>();
        if (processed instanceof List<?> list) {
            for (Object item : list) {
                if (item != null) {
                    callbacks.add(String.valueOf(item));
                }
            }
        }
        if (!callbacks.contains(callbackFingerprint)) {
            callbacks.add(callbackFingerprint);
        }
        actionEntry.put("processedCallbacks", callbacks);
    }

    private String resolveActionKeyByCorrelation(ManagedObject object, String correlationKey) {
        if (object.getAsyncActionRegistry() == null) {
            throw new IllegalArgumentException("async action not found");
        }
        return object.getAsyncActionRegistry().stream()
                .filter(registration -> registration != null && correlationKey.equals(registration.getCorrelationKey()))
                .map(com.cyancoder.bpm.domain.AsyncActionRegistration::getActionKey)
                .findFirst()
                .orElseThrow();
    }

    private void markAsyncRegistrationStatus(ManagedObject object, String actionKey, String status) {
        if (object.getAsyncActionRegistry() == null) {
            return;
        }
        for (var registration : object.getAsyncActionRegistry()) {
            if (registration != null && actionKey.equals(registration.getActionKey())) {
                registration.setStatus(status);
            }
        }
    }
}
