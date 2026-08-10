package com.cyancoder.aiorchestrator.service;

import com.cyancoder.aiorchestrator.domain.*;
import com.cyancoder.aiorchestrator.repo.*;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ProjectReleaseServiceTest {
    @Test void rejectsDraftOutsideActiveTenantAndSite() {
        ProjectReleaseRepository releases=mock(ProjectReleaseRepository.class);
        ClientAppDraftRepository drafts=mock(ClientAppDraftRepository.class);
        ProvisioningRunRepository runs=mock(ProvisioningRunRepository.class);
        ClientAppDraft draft=new ClientAppDraft();draft.setDraftId("draft-1");draft.setTenantKey("tenant-a");draft.setSiteKey("site-a");
        when(drafts.findByDraftId("draft-1")).thenReturn(Optional.of(draft));
        var service=new ProjectReleaseService(releases,drafts,runs);
        assertThatThrownBy(()->service.list("draft-1","tenant-b","site-a")).isInstanceOf(java.util.NoSuchElementException.class);
        verifyNoInteractions(releases);
    }
}
