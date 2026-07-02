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
import com.cyancoder.bpm.domain.AutomationBlockExecution;
import com.cyancoder.bpm.domain.AutomationFailurePolicy;
import com.cyancoder.bpm.domain.DynamicFlowDefinition;
import com.cyancoder.bpm.domain.FlowActionConfig;
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
                                                   String blockKey,
                                                   AsyncActionCallbackRequest request,
                                                   TransitionActorContext actorContext,
                                                   String callbackFingerprint) {
        ManagedObject object = findById(scope, objectId);
        AutomationBlockExecution block = automationBlockEntry(object, blockKey);
        if (isDuplicateCallback(block, callbackFingerprint)) {
            object.getAuditLog().add("duplicate automation callback " + blockKey + " ignored at=" + Instant.now());
            return object;
        }
        Map<String, Object> callbackPayload = request == null || request.payload() == null ? Map.of() : request.payload();
        String callbackStatus = request == null || request.status() == null || request.status().isBlank() ? "SUCCESS" : request.status().trim().toUpperCase();
        CallApiActionSupport.applyResponseMappingsFromCallback(object, callbackPayload, block.getOutputMappings());
        if (block.getStoreOutputAt() != null && !block.getStoreOutputAt().isBlank()) {
            ActionPayloadSupport.setPayloadPath(object, block.getStoreOutputAt(), callbackPayload);
        }
        applyAutomationExecutionStores(object, block, stringValue(callbackPayload.get("executionId")), callbackStatus, callbackPayload);
        rememberProcessedCallback(block, callbackFingerprint);
        block.setStatus(callbackStatus);
        block.setFinishedAt(Instant.now());
        block.setUpdatedAt(Instant.now());
        if (callbackPayload.get("snapshot") instanceof Map<?, ?> map) {
            block.setSnapshot(new LinkedHashMap<>((Map<String, Object>) map));
        }
        block.setOutput(new LinkedHashMap<>(callbackPayload));
        if ("FAILED".equalsIgnoreCase(callbackStatus) || "TIMED_OUT".equalsIgnoreCase(callbackStatus) || "CANCELLED".equalsIgnoreCase(callbackStatus)) {
            block.setError(new LinkedHashMap<>(callbackPayload));
        } else {
            block.setError(new LinkedHashMap<>());
        }
        object.getAuditLog().add("automation callback " + blockKey + " received at=" + Instant.now());
        object.setUpdatedAt(Instant.now());
        managedObjectRepository.save(object);

        if ("FAILED".equalsIgnoreCase(callbackStatus) && block.getFailurePolicy() == AutomationFailurePolicy.RETRY && canRetry(block)) {
            restartAutomationBlock(scope, object, block);
            managedObjectRepository.save(object);
            return object;
        }

        DynamicFlowDefinition definition = flowDefinitionService.getActiveByFlowKey(scope, object.getFlowKey());
        FlowState currentState = findState(definition, object.getState());
        if (isAutomaticState(currentState) && !hasPendingBlockingAutomationForCurrentState(object, currentState)) {
            String requestedNextState = "FAILED".equalsIgnoreCase(callbackStatus) || "TIMED_OUT".equalsIgnoreCase(callbackStatus) || "CANCELLED".equalsIgnoreCase(callbackStatus)
                    ? firstNonBlank(block.getNextStateOnFailure(), request == null ? null : request.nextState())
                    : firstNonBlank(block.getNextStateOnSuccess(), request == null ? null : request.nextState());
            return transit(scope,
                    objectId,
                    requestedNextState,
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
        ManagedObject object = managedObjectRepository.findFirstByTenantKeyAndSiteKeyAndAutomationBlockRegistryCorrelationKey(scope.tenantKey(), scope.siteKey(), correlationKey)
                .orElseThrow();
        String blockKey = resolveBlockKeyByCorrelation(object, correlationKey);
        return acceptAsyncActionCallback(scope, object.getId(), blockKey, request, actorContext, callbackFingerprint);
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
        while (isAutomaticState(findState(definition, object.getState())) && !hasPendingBlockingAutomationForCurrentState(object, findState(definition, object.getState()))) {
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

    private boolean hasPendingBlockingAutomationForCurrentState(ManagedObject object, FlowState state) {
        if (state == null || !state.waitForAutomation() || object.getAutomationBlockRegistry() == null) {
            return false;
        }
        for (var block : object.getAutomationBlockRegistry()) {
            if (block != null
                    && object.getState() != null
                    && object.getState().equals(block.getStateId())
                    && block.isWaitForCompletion()
                    && ("PENDING".equalsIgnoreCase(block.getStatus()) || "RUNNING".equalsIgnoreCase(block.getStatus()))) {
                return true;
            }
        }
        return false;
    }

    private AutomationBlockExecution automationBlockEntry(ManagedObject object, String blockKey) {
        if (object.getAutomationBlockRegistry() == null) {
            throw new IllegalArgumentException("automation block not found");
        }
        return object.getAutomationBlockRegistry().stream()
                .filter(item -> item != null && blockKey.equals(item.getBlockKey()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("automation block not found"));
    }

    private boolean isDuplicateCallback(AutomationBlockExecution block, String callbackFingerprint) {
        if (callbackFingerprint == null || callbackFingerprint.isBlank()) {
            return false;
        }
        return block.getProcessedCallbacks() != null && block.getProcessedCallbacks().stream().anyMatch(callbackFingerprint::equals);
    }

    private void rememberProcessedCallback(AutomationBlockExecution block, String callbackFingerprint) {
        if (callbackFingerprint == null || callbackFingerprint.isBlank()) {
            return;
        }
        List<String> callbacks = new ArrayList<>(block.getProcessedCallbacks() == null ? List.of() : block.getProcessedCallbacks());
        if (!callbacks.contains(callbackFingerprint)) {
            callbacks.add(callbackFingerprint);
        }
        block.setProcessedCallbacks(callbacks);
    }

    private String resolveBlockKeyByCorrelation(ManagedObject object, String correlationKey) {
        if (object.getAutomationBlockRegistry() == null) {
            throw new IllegalArgumentException("automation block not found");
        }
        return object.getAutomationBlockRegistry().stream()
                .filter(block -> block != null && correlationKey.equals(block.getCorrelationKey()))
                .map(AutomationBlockExecution::getBlockKey)
                .findFirst()
                .orElseThrow();
    }

    private boolean canRetry(AutomationBlockExecution block) {
        return block.getRetryCount() != null && block.getMaxRetries() != null && block.getRetryCount() < block.getMaxRetries();
    }

    private void restartAutomationBlock(BpmScope scope, ManagedObject object, AutomationBlockExecution block) {
        block.setRetryCount((block.getRetryCount() == null ? 0 : block.getRetryCount()) + 1);
        block.setStatus("PENDING");
        block.setFinishedAt(null);
        block.setUpdatedAt(Instant.now());
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("blockKey", block.getBlockKey());
        params.put("automationFlowKey", block.getAutomationFlowKey());
        params.put("executionMode", block.getExecutionMode().name());
        params.put("failurePolicy", block.getFailurePolicy().name());
        params.put("serviceKey", block.getServiceKey());
        params.put("path", block.getPath());
        params.put("method", firstNonBlank(block.getMethod(), "POST"));
        params.put("body", block.getRequestBody());
        params.put("inlineFragment", block.getInlineFragment());
        params.put("responseMappings", block.getStartResponseMappings());
        params.put("storeFullResponseAt", block.getStoreStartResponseAt());
        params.put("callbackResponseMappings", block.getOutputMappings());
        params.put("callbackStoreFullResponseAt", block.getStoreOutputAt());
        params.put("storeExecutionIdAt", block.getStoreExecutionIdAt());
        params.put("storeStatusAt", block.getStoreStatusAt());
        params.put("storeVariablesAt", block.getStoreVariablesAt());
        params.put("storeFullExecutionAt", block.getStoreFullExecutionAt());
        params.put("storeErrorAt", block.getStoreErrorAt());
        params.put("maxRetries", block.getMaxRetries());
        params.put("timeoutSeconds", block.getTimeoutSeconds());
        params.put("nextStateOnSuccess", block.getNextStateOnSuccess());
        params.put("nextStateOnFailure", block.getNextStateOnFailure());
        params.put("waitForCompletion", block.isWaitForCompletion());
        params.put("correlationKey", block.getCorrelationKey());
        FlowActionConfig action = new FlowActionConfig(com.cyancoder.bpm.domain.ActionType.RUN_AUTOMATION_BLOCK, params);
        actionExecutor.execute(List.of(action), object, scope, "system-retry");
    }

    private void applyAutomationExecutionStores(ManagedObject object,
                                                AutomationBlockExecution block,
                                                String executionId,
                                                String status,
                                                Map<String, Object> payload) {
        applyOptionalStore(object, block.getStoreExecutionIdAt(), executionId);
        applyOptionalStore(object, block.getStoreStatusAt(), status);
        Object output = payload == null ? null : payload.get("output");
        applyOptionalStore(object, block.getStoreVariablesAt(), output == null ? payload : output);
        applyOptionalStore(object, block.getStoreFullExecutionAt(), payload);
        Object error = payload == null ? null : payload.get("error");
        if (error == null) {
            removeOptionalStore(object, block.getStoreErrorAt());
        } else {
            applyOptionalStore(object, block.getStoreErrorAt(), error);
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

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
