package com.cyancoder.bpm.api;

import com.cyancoder.bpm.api.dto.CreateManagedObjectRequest;
import com.cyancoder.bpm.api.dto.FlowScopeResolver;
import com.cyancoder.bpm.api.dto.ManagedObjectActiveFormResponse;
import com.cyancoder.bpm.api.dto.ManagedObjectAttachmentRequest;
import com.cyancoder.bpm.api.dto.ManagedObjectCommentRequest;
import com.cyancoder.bpm.api.dto.ManagedObjectFormSubmissionResponse;
import com.cyancoder.bpm.api.dto.SubmitManagedObjectFormRequest;
import com.cyancoder.bpm.api.dto.TransitionRequest;
import com.cyancoder.bpm.api.dto.TransitionOptionResponse;
import com.cyancoder.bpm.domain.ManagedObject;
import com.cyancoder.bpm.domain.ManagedObjectAttachment;
import com.cyancoder.bpm.domain.ManagedObjectComment;
import com.cyancoder.bpm.service.ActorContextResolver;
import com.cyancoder.bpm.service.ManagedObjectCollaborationService;
import com.cyancoder.bpm.service.ObjectFlowService;
import com.cyancoder.bpm.service.BpmAssignmentDirectoryService;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/endpoint/bpm/managed-objects")
public class EndpointManagedObjectFlowController {
    private final ObjectFlowService objectFlowService;
    private final ActorContextResolver actorContextResolver;
    private final ManagedObjectCollaborationService collaborationService;
    private final BpmAssignmentDirectoryService assignmentDirectory;

    public EndpointManagedObjectFlowController(ObjectFlowService objectFlowService, ActorContextResolver actorContextResolver,
                                               ManagedObjectCollaborationService collaborationService,
                                               BpmAssignmentDirectoryService assignmentDirectory) {
        this.objectFlowService = objectFlowService;
        this.actorContextResolver = actorContextResolver;
        this.collaborationService = collaborationService;
        this.assignmentDirectory = assignmentDirectory;
    }

    @GetMapping("/assignment-targets")
    @PreAuthorize("@platformAuthorizationService.hasAnyPermission('bpm.transition','bpm.manage','operations:*')")
    public java.util.List<com.cyancoder.bpm.api.dto.AssignmentTargetResponse> assignmentTargets(
            @RequestHeader("X-Tenant-Key") String tenantKey,
            @RequestParam(defaultValue = "USER") com.cyancoder.bpm.domain.AssigneeType type,
            @RequestParam(required = false) String query) {
        return assignmentDirectory.search(tenantKey, type, query);
    }

    @GetMapping("/cartable")
    @PreAuthorize("@platformAuthorizationService.hasAnyPermission('bpm.read','bpm.transition','bpm.manage','operations:*')")
    public com.cyancoder.bpm.api.dto.ManagedObjectQueueResponse cartable(
            @RequestHeader("X-Tenant-Key") String tenantKey,
            @RequestHeader(value="X-Site-Key",required=false) String siteKey,
            @RequestParam(defaultValue="ASSIGNED") String view,
            @RequestParam(required=false) String state,
            @RequestParam(required=false) String priority,
            @RequestParam(required=false) Boolean overdue,
            @RequestParam(required=false) String query,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size,
            Authentication authentication) {
        return objectFlowService.cartable(FlowScopeResolver.fromHeaders(tenantKey,siteKey), actorContextResolver.fromAuthentication(authentication), view, state, priority, overdue, query, page, size);
    }

    @GetMapping
    @PreAuthorize("@platformAuthorizationService.hasAnyPermission('bpm.read','bpm.transition','bpm.manage','operations:*')")
    public List<ManagedObject> list(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey
    ) {
        return objectFlowService.findAll(FlowScopeResolver.fromHeaders(tenantKey, siteKey));
    }

    @GetMapping("/assigned-to-me")
    @PreAuthorize("@platformAuthorizationService.hasAnyPermission('bpm.read','bpm.transition','bpm.manage','operations:*')")
    public List<ManagedObject> getAssignedToCurrentUser(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            Authentication authentication
    ) {
        var actor = actorContextResolver.fromAuthentication(authentication);
        return objectFlowService.findAllAssignedToActor(FlowScopeResolver.fromHeaders(tenantKey, siteKey), actor);
    }

    @GetMapping("/visible-to-me")
    @PreAuthorize("@platformAuthorizationService.hasAnyPermission('bpm.read','bpm.transition','bpm.manage','operations:*')")
    public List<ManagedObject> getVisibleToCurrentUser(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            Authentication authentication
    ) {
        return objectFlowService.findAllVisibleToActor(FlowScopeResolver.fromHeaders(tenantKey, siteKey), actorContextResolver.fromAuthentication(authentication));
    }

    @PostMapping
    @PreAuthorize("@platformAuthorizationService.hasAnyPermission('bpm.manage','operations:*')")
    public ManagedObject createAndStart(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @RequestBody CreateManagedObjectRequest request,
            Authentication authentication
    ) {
        return objectFlowService.createAndStart(FlowScopeResolver.fromHeaders(tenantKey, siteKey), request, actorContextResolver.fromAuthentication(authentication));
    }

    @PostMapping("/{objectId}/transitions")
    @PreAuthorize("@platformAuthorizationService.hasAnyPermission('bpm.transition','bpm.manage','operations:*')")
    public ManagedObject transition(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable String objectId,
            @RequestBody TransitionRequest request,
            Authentication authentication
    ) {
        return objectFlowService.transit(FlowScopeResolver.fromHeaders(tenantKey, siteKey), objectId, request.nextState(), actorContextResolver.fromAuthentication(authentication), request.context());
    }

    @GetMapping("/{objectId}/transitions")
    @PreAuthorize("@platformAuthorizationService.hasAnyPermission('bpm.read','bpm.transition','bpm.manage','operations:*')")
    public List<TransitionOptionResponse> availableTransitions(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable String objectId,
            Authentication authentication
    ) {
        return objectFlowService.availableTransitions(
                FlowScopeResolver.fromHeaders(tenantKey, siteKey),
                objectId,
                actorContextResolver.fromAuthentication(authentication),
                null
        );
    }

    @GetMapping("/{objectId}/active-form")
    @PreAuthorize("@platformAuthorizationService.hasAnyPermission('bpm.read','bpm.transition','bpm.manage','operations:*')")
    public ManagedObjectActiveFormResponse getActiveForm(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable String objectId
    ) {
        return objectFlowService.getActiveForm(FlowScopeResolver.fromHeaders(tenantKey, siteKey), objectId);
    }

    @PostMapping("/{objectId}/active-form/submissions")
    @PreAuthorize("@platformAuthorizationService.hasAnyPermission('bpm.transition','bpm.manage','operations:*')")
    public ManagedObjectFormSubmissionResponse submitActiveForm(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable String objectId,
            @RequestBody SubmitManagedObjectFormRequest request,
            Authentication authentication
    ) {
        return objectFlowService.submitActiveForm(FlowScopeResolver.fromHeaders(tenantKey, siteKey), objectId, request, actorContextResolver.fromAuthentication(authentication));
    }

    @PostMapping("/{objectId}/comments")
    @PreAuthorize("@platformAuthorizationService.hasAnyPermission('bpm.transition','bpm.manage','operations:*')")
    public ManagedObjectComment addComment(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable String objectId,
            @RequestBody ManagedObjectCommentRequest request,
            Authentication authentication
    ) {
        return collaborationService.addComment(FlowScopeResolver.fromHeaders(tenantKey, siteKey), objectId, request,
                actorContextResolver.fromAuthentication(authentication));
    }

    @GetMapping("/{objectId}/comments")
    @PreAuthorize("@platformAuthorizationService.hasAnyPermission('bpm.read','bpm.transition','bpm.manage','operations:*')")
    public List<ManagedObjectComment> comments(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable String objectId,
            Authentication authentication
    ) {
        return collaborationService.comments(FlowScopeResolver.fromHeaders(tenantKey, siteKey), objectId,
                actorContextResolver.fromAuthentication(authentication));
    }

    @PostMapping("/{objectId}/attachments")
    @PreAuthorize("@platformAuthorizationService.hasAnyPermission('bpm.transition','bpm.manage','operations:*')")
    public ManagedObjectAttachment addAttachment(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable String objectId,
            @RequestBody ManagedObjectAttachmentRequest request,
            Authentication authentication
    ) {
        return collaborationService.addAttachment(FlowScopeResolver.fromHeaders(tenantKey, siteKey), objectId, request,
                actorContextResolver.fromAuthentication(authentication));
    }

    @GetMapping("/{objectId}/attachments")
    @PreAuthorize("@platformAuthorizationService.hasAnyPermission('bpm.read','bpm.transition','bpm.manage','operations:*')")
    public List<ManagedObjectAttachment> attachments(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable String objectId,
            Authentication authentication
    ) {
        return collaborationService.attachments(FlowScopeResolver.fromHeaders(tenantKey, siteKey), objectId,
                actorContextResolver.fromAuthentication(authentication));
    }

    @GetMapping("/{objectId}")
    @PreAuthorize("@platformAuthorizationService.hasAnyPermission('bpm.read','bpm.transition','bpm.manage','operations:*')")
    public ManagedObject get(
            @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
            @RequestHeader(value = "X-Site-Key", required = false) String siteKey,
            @PathVariable String objectId
    ) {
        return objectFlowService.findById(FlowScopeResolver.fromHeaders(tenantKey, siteKey), objectId);
    }

    @PutMapping("/{objectId}/assignment")
    @PreAuthorize("@platformAuthorizationService.hasAnyPermission('bpm.transition','bpm.manage','operations:*')")
    public ManagedObject assign(@RequestHeader(value="X-Tenant-Key",required=false) String tenantKey,@RequestHeader(value="X-Site-Key",required=false) String siteKey,@PathVariable String objectId,@Valid @RequestBody com.cyancoder.bpm.api.dto.AssignManagedObjectRequest request,Authentication authentication){return objectFlowService.assign(FlowScopeResolver.fromHeaders(tenantKey,siteKey),objectId,request.assignee(),request.assigneeType(),actorContextResolver.fromAuthentication(authentication));}

    @PostMapping("/{objectId}/lock")
    @PreAuthorize("@platformAuthorizationService.hasAnyPermission('bpm.transition','bpm.manage','operations:*')")
    public ManagedObject lock(@RequestHeader(value="X-Tenant-Key",required=false) String tenantKey,@RequestHeader(value="X-Site-Key",required=false) String siteKey,@PathVariable String objectId,Authentication authentication){return objectFlowService.lock(FlowScopeResolver.fromHeaders(tenantKey,siteKey),objectId,true,actorContextResolver.fromAuthentication(authentication));}

    @PostMapping("/{objectId}/unlock")
    @PreAuthorize("@platformAuthorizationService.hasAnyPermission('bpm.transition','bpm.manage','operations:*')")
    public ManagedObject unlock(@RequestHeader(value="X-Tenant-Key",required=false) String tenantKey,@RequestHeader(value="X-Site-Key",required=false) String siteKey,@PathVariable String objectId,Authentication authentication){return objectFlowService.lock(FlowScopeResolver.fromHeaders(tenantKey,siteKey),objectId,false,actorContextResolver.fromAuthentication(authentication));}
}
