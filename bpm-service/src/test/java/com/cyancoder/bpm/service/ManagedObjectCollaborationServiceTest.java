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
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ManagedObjectCollaborationServiceTest {

    @Test
    void defaultsCommentVisibilityToRoleAssigneeAndKeepsAttachmentsPublic() {
        ObjectFlowService objectFlowService = mock(ObjectFlowService.class);
        ManagedObjectCommentRepository comments = mock(ManagedObjectCommentRepository.class);
        ManagedObjectAttachmentRepository attachments = mock(ManagedObjectAttachmentRepository.class);
        ManagedObject object = new ManagedObject();
        object.setId("obj-1");
        object.setState("review");
        object.setAssignee("ROLE_MANAGER");
        object.setAssigneeType(AssigneeType.ROLE);
        when(objectFlowService.findById(new BpmScope("tenant", "site"), "obj-1")).thenReturn(object);
        when(comments.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(attachments.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ManagedObjectCollaborationService service = new ManagedObjectCollaborationService(objectFlowService, comments, attachments);
        ManagedObjectComment comment = service.addComment(new BpmScope("tenant", "site"), "obj-1",
                new ManagedObjectCommentRequest(null, "Please review", null, null, null, null, Map.of()),
                new TransitionActorContext("author", Set.of(), Set.of()));
        ManagedObjectAttachment attachment = service.addAttachment(new BpmScope("tenant", "site"), "obj-1",
                new ManagedObjectAttachmentRequest(null, "asset-1", "doc.pdf", null, "application/pdf", 10L,
                        null, null, null, null, Map.of()),
                new TransitionActorContext("author", Set.of(), Set.of()));

        assertThat(comment.getVisibleToRoles()).containsExactly("ROLE_MANAGER");
        assertThat(attachment.getAssetKey()).isEqualTo("asset-1");

        when(comments.findAllByObjectIdOrderByCreatedAtAsc("obj-1")).thenReturn(List.of(comment));
        when(attachments.findAllByObjectIdOrderByCreatedAtAsc("obj-1")).thenReturn(List.of(attachment));
        assertThat(service.comments(new BpmScope("tenant", "site"), "obj-1",
                new TransitionActorContext("manager", Set.of(), Set.of("ROLE_MANAGER")))).containsExactly(comment);
        assertThat(service.attachments(new BpmScope("tenant", "site"), "obj-1",
                new TransitionActorContext("viewer", Set.of(), Set.of()))).containsExactly(attachment);
    }
}
