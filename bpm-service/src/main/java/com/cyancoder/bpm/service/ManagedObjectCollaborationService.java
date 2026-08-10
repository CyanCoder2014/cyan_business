package com.cyancoder.bpm.service;

import com.cyancoder.bpm.api.dto.BpmScope;
import com.cyancoder.bpm.api.dto.ManagedObjectAttachmentRequest;
import com.cyancoder.bpm.api.dto.ManagedObjectCommentRequest;
import com.cyancoder.bpm.api.dto.TransitionActorContext;
import com.cyancoder.bpm.domain.AssigneeType;
import com.cyancoder.bpm.domain.ManagedObject;
import com.cyancoder.bpm.domain.ManagedObjectAttachment;
import com.cyancoder.bpm.domain.ManagedObjectComment;
import com.cyancoder.bpm.repo.ManagedObjectAttachmentRepository;
import com.cyancoder.bpm.repo.ManagedObjectCommentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ManagedObjectCollaborationService {
    private final ObjectFlowService objectFlowService;
    private final ManagedObjectCommentRepository commentRepository;
    private final ManagedObjectAttachmentRepository attachmentRepository;
    private final InternalServiceHttpSupport http;

    public ManagedObjectCollaborationService(
            ObjectFlowService objectFlowService,
            ManagedObjectCommentRepository commentRepository,
            ManagedObjectAttachmentRepository attachmentRepository,
            InternalServiceHttpSupport http
    ) {
        this.objectFlowService = objectFlowService;
        this.commentRepository = commentRepository;
        this.attachmentRepository = attachmentRepository;
        this.http = http;
    }

    public ManagedObjectComment addComment(BpmScope scope, String objectId, ManagedObjectCommentRequest request, TransitionActorContext actor) {
        ManagedObject object = objectFlowService.findById(scope, objectId);
        objectFlowService.assertCanRead(object, actor);
        if (request == null || request.body() == null || request.body().isBlank()) {
            throw new IllegalArgumentException("comment body is required");
        }
        Set<String> users = copy(request.visibleToUserIds());
        Set<String> roles = copy(request.visibleToRoles());
        Set<String> groups = copy(request.visibleToGroups());
        if (users.isEmpty() && roles.isEmpty() && groups.isEmpty() && object.getAssignee() != null) {
            addAssigneeTarget(object, users, roles, groups);
        }
        ManagedObjectComment comment = new ManagedObjectComment();
        comment.setObjectId(objectId);
        comment.setStateId(firstNonBlank(request.stateId(), object.getState()));
        comment.setAuthorUserId(actorUser(actor));
        comment.setBody(request.body().trim());
        comment.setVisibleToUserIds(users);
        comment.setVisibleToRoles(roles);
        comment.setVisibleToGroups(groups);
        comment.setVisibleUntilState(normalize(request.visibleUntilState()));
        comment.setMetadata(request.metadata() == null ? Map.of() : request.metadata());
        comment.setCreatedAt(Instant.now());
        return commentRepository.save(comment);
    }

    public List<ManagedObjectComment> comments(BpmScope scope, String objectId, TransitionActorContext actor) {
        ManagedObject object = objectFlowService.findById(scope, objectId);
        objectFlowService.assertCanRead(object, actor);
        return commentRepository.findAllByObjectIdOrderByCreatedAtAsc(objectId).stream()
                .filter(comment -> visible(comment.getAuthorUserId(), comment.getVisibleToUserIds(), comment.getVisibleToRoles(),
                        comment.getVisibleToGroups(), comment.getVisibleUntilState(), object, actor, false))
                .toList();
    }

    public ManagedObjectAttachment addAttachment(BpmScope scope, String objectId, ManagedObjectAttachmentRequest request, TransitionActorContext actor) {
        ManagedObject object = objectFlowService.findById(scope, objectId);
        objectFlowService.assertCanRead(object, actor);
        if (request == null || request.assetKey() == null || request.assetKey().isBlank()) {
            throw new IllegalArgumentException("attachment assetKey is required");
        }
        ManagedObjectAttachment attachment = new ManagedObjectAttachment();
        attachment.setObjectId(objectId);
        attachment.setStateId(firstNonBlank(request.stateId(), object.getState()));
        attachment.setAuthorUserId(actorUser(actor));
        attachment.setAssetKey(request.assetKey().trim());
        attachment.setFileName(normalize(request.fileName()));
        attachment.setDownloadUrl(normalize(request.downloadUrl()));
        attachment.setContentType(normalize(request.contentType()));
        attachment.setSizeBytes(request.sizeBytes());
        attachment.setVisibleToUserIds(copy(request.visibleToUserIds()));
        attachment.setVisibleToRoles(copy(request.visibleToRoles()));
        attachment.setVisibleToGroups(copy(request.visibleToGroups()));
        attachment.setVisibleUntilState(normalize(request.visibleUntilState()));
        attachment.setMetadata(request.metadata() == null ? Map.of() : request.metadata());
        attachment.setCreatedAt(Instant.now());
        ManagedObjectAttachment saved=attachmentRepository.save(attachment);
        String referenceKey = saved.getId() == null ? objectId + ":" + saved.getAssetKey() : saved.getId();
        http.exchange("media-service","/internal/media/assets/"+java.net.URLEncoder.encode(saved.getAssetKey(),java.nio.charset.StandardCharsets.UTF_8)+"/references",org.springframework.http.HttpMethod.PUT,Map.of("ownerService","bpm-service","ownerType","BPM_ATTACHMENT","ownerKey",referenceKey,"fieldPath","assetKey"),scope.tenantKey(),scope.siteKey(),Object.class);
        return saved;
    }

    public List<ManagedObjectAttachment> attachments(BpmScope scope, String objectId, TransitionActorContext actor) {
        ManagedObject object = objectFlowService.findById(scope, objectId);
        objectFlowService.assertCanRead(object, actor);
        return attachmentRepository.findAllByObjectIdOrderByCreatedAtAsc(objectId).stream()
                .filter(attachment -> visible(attachment.getAuthorUserId(), attachment.getVisibleToUserIds(), attachment.getVisibleToRoles(),
                        attachment.getVisibleToGroups(), attachment.getVisibleUntilState(), object, actor, true))
                .toList();
    }

    private boolean visible(String author, Set<String> users, Set<String> roles, Set<String> groups,
                            String visibleUntilState, ManagedObject object, TransitionActorContext actor, boolean publicWhenUnscoped) {
        if (actor == null) {
            return false;
        }
        if (actorUser(actor).equals(author)) {
            return true;
        }
        if (visibleUntilState != null && !visibleUntilState.equals(object.getState())) {
            return false;
        }
        if (publicWhenUnscoped && users.isEmpty() && roles.isEmpty() && groups.isEmpty()) {
            return true;
        }
        return users.contains(actor.userId())
                || actor.rolesOrEmpty().stream().anyMatch(roles::contains)
                || actor.groupsOrEmpty().stream().anyMatch(groups::contains);
    }

    private void addAssigneeTarget(ManagedObject object, Set<String> users, Set<String> roles, Set<String> groups) {
        AssigneeType type = object.getAssigneeType();
        switch (type) {
            case USER -> users.add(object.getAssignee());
            case ROLE -> roles.add(object.getAssignee());
            case GROUP -> groups.add(object.getAssignee());
        }
    }

    private Set<String> copy(Set<String> values) {
        return values == null ? new LinkedHashSet<>() : new LinkedHashSet<>(values);
    }

    private String actorUser(TransitionActorContext actor) {
        return actor == null || actor.userId() == null || actor.userId().isBlank() ? "system" : actor.userId();
    }

    private String firstNonBlank(String first, String second) {
        return normalize(first) == null ? second : normalize(first);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
