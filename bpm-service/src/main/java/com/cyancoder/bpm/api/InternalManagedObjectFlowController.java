package com.cyancoder.bpm.api;

import com.cyancoder.bpm.api.dto.CreateManagedObjectRequest;
import com.cyancoder.bpm.api.dto.FlowScopeResolver;
import com.cyancoder.bpm.api.dto.ManagedObjectActiveFormResponse;
import com.cyancoder.bpm.api.dto.ManagedObjectFormSubmissionResponse;
import com.cyancoder.bpm.api.dto.SubmitManagedObjectFormRequest;
import com.cyancoder.bpm.api.dto.TransitionRequest;
import com.cyancoder.bpm.api.dto.TransitionOptionResponse;
import com.cyancoder.bpm.domain.ManagedObject;
import com.cyancoder.bpm.service.ActorContextResolver;
import com.cyancoder.bpm.service.ObjectFlowService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/bpm/managed-objects")
public class InternalManagedObjectFlowController {
    private final ObjectFlowService objectFlowService;
    private final ActorContextResolver actorContextResolver;

    public InternalManagedObjectFlowController(ObjectFlowService objectFlowService, ActorContextResolver actorContextResolver) {
        this.objectFlowService = objectFlowService;
        this.actorContextResolver = actorContextResolver;
    }

    @GetMapping
    public List<ManagedObject> list(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey
    ) {
        return objectFlowService.findAll(FlowScopeResolver.fromHeaders(tenantKey, siteKey));
    }

    @PostMapping
    public ManagedObject createAndStart(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @RequestHeader(value = "X-Actor-User", required = false) String actorUser,
            @RequestHeader(value = "X-Actor-Roles", required = false) String actorRoles,
            @RequestHeader(value = "X-Actor-Groups", required = false) String actorGroups,
            @RequestBody CreateManagedObjectRequest request
    ) {
        return objectFlowService.createAndStart(
                FlowScopeResolver.fromHeaders(tenantKey, siteKey),
                request,
                actorContextResolver.fromInternalHeaders(actorUser, actorRoles, actorGroups)
        );
    }

    @PostMapping("/{objectId}/transitions")
    public ManagedObject transition(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @RequestHeader(value = "X-Actor-User", required = false) String actorUser,
            @RequestHeader(value = "X-Actor-Roles", required = false) String actorRoles,
            @RequestHeader(value = "X-Actor-Groups", required = false) String actorGroups,
            @PathVariable String objectId,
            @RequestBody TransitionRequest request
    ) {
        return objectFlowService.transit(
                FlowScopeResolver.fromHeaders(tenantKey, siteKey),
                objectId,
                request.nextState(),
                actorContextResolver.fromInternalHeaders(actorUser, actorRoles, actorGroups),
                request.context()
        );
    }

    @GetMapping("/{objectId}/transitions")
    public List<TransitionOptionResponse> availableTransitions(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @RequestHeader(value = "X-Actor-User", required = false) String actorUser,
            @RequestHeader(value = "X-Actor-Roles", required = false) String actorRoles,
            @RequestHeader(value = "X-Actor-Groups", required = false) String actorGroups,
            @PathVariable String objectId
    ) {
        return objectFlowService.availableTransitions(
                FlowScopeResolver.fromHeaders(tenantKey, siteKey),
                objectId,
                actorContextResolver.fromInternalHeaders(actorUser, actorRoles, actorGroups),
                null
        );
    }

    @GetMapping("/{objectId}/active-form")
    public ManagedObjectActiveFormResponse getActiveForm(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable String objectId
    ) {
        return objectFlowService.getActiveForm(FlowScopeResolver.fromHeaders(tenantKey, siteKey), objectId);
    }

    @PostMapping("/{objectId}/active-form/submissions")
    public ManagedObjectFormSubmissionResponse submitActiveForm(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @RequestHeader(value = "X-Actor-User", required = false) String actorUser,
            @RequestHeader(value = "X-Actor-Roles", required = false) String actorRoles,
            @RequestHeader(value = "X-Actor-Groups", required = false) String actorGroups,
            @PathVariable String objectId,
            @RequestBody SubmitManagedObjectFormRequest request
    ) {
        return objectFlowService.submitActiveForm(
                FlowScopeResolver.fromHeaders(tenantKey, siteKey),
                objectId,
                request,
                actorContextResolver.fromInternalHeaders(actorUser, actorRoles, actorGroups)
        );
    }

    @GetMapping("/{objectId}")
    public ManagedObject get(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable String objectId
    ) {
        return objectFlowService.findById(FlowScopeResolver.fromHeaders(tenantKey, siteKey), objectId);
    }
}
