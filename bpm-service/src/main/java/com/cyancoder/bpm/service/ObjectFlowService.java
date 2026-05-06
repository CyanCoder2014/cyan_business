package com.cyancoder.bpm.service;

import com.cyancoder.bpm.api.dto.BpmScope;
import com.cyancoder.bpm.api.dto.CreateManagedObjectRequest;
import com.cyancoder.bpm.api.dto.FormSubmissionSyncRequest;
import com.cyancoder.bpm.api.dto.FormSubmissionSyncResponse;
import com.cyancoder.bpm.api.dto.ManagedObjectActiveFormResponse;
import com.cyancoder.bpm.api.dto.ManagedObjectFormSubmissionResponse;
import com.cyancoder.bpm.api.dto.SubmitManagedObjectFormRequest;
import com.cyancoder.bpm.api.dto.TransitionActorContext;
import com.cyancoder.bpm.domain.DynamicFlowDefinition;
import com.cyancoder.bpm.domain.FlowState;
import com.cyancoder.bpm.domain.FlowTransition;
import com.cyancoder.bpm.domain.ManagedObject;
import com.cyancoder.bpm.domain.TransitionHistoryEntry;
import com.cyancoder.bpm.repo.ManagedObjectRepository;
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

    public ObjectFlowService(ManagedObjectRepository managedObjectRepository,
                             FlowDefinitionService flowDefinitionService,
                             FlowTransitionConditionEvaluator transitionConditionEvaluator,
                             DynamicFlowIntegrationClient integrationClient,
                             FlowActionExecutor actionExecutor) {
        this.managedObjectRepository = managedObjectRepository;
        this.flowDefinitionService = flowDefinitionService;
        this.transitionConditionEvaluator = transitionConditionEvaluator;
        this.integrationClient = integrationClient;
        this.actionExecutor = actionExecutor;
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
        return managedObjectRepository.save(object);
    }

    public ManagedObject transit(BpmScope scope, String objectId, String nextState, TransitionActorContext actorContext, Map<String, Object> context) {
        ManagedObject object = findById(scope, objectId);
        DynamicFlowDefinition definition = flowDefinitionService.getActiveByFlowKey(scope, object.getFlowKey());
        FlowTransition transition = resolveTransition(definition, object, nextState, actorContext, context == null ? Map.of() : context);
        String actor = actor(actorContext);
        String previousState = object.getState();

        appendTransitionHistory(object, transition, previousState, transition.toState(), actor, context);
        object.setState(transition.toState());
        FlowState newState = findState(definition, transition.toState());
        syncStatePayload(object, newState);
        applyStateEffects(scope, object, newState, actor);
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
            if (!transitionConditionEvaluator.evaluate(transition, object.getPayload(), context)) {
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
}

