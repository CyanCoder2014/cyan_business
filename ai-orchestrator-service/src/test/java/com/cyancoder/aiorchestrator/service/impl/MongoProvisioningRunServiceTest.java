package com.cyancoder.aiorchestrator.service.impl;

import com.cyancoder.aiorchestrator.api.dto.ProvisionDraftRequest;
import com.cyancoder.aiorchestrator.api.dto.ProvisioningRunDto;
import com.cyancoder.aiorchestrator.domain.ClientAppDraft;
import com.cyancoder.aiorchestrator.domain.ProvisioningRun;
import com.cyancoder.aiorchestrator.domain.ProvisioningRunStatus;
import com.cyancoder.aiorchestrator.repo.ClientAppDraftRepository;
import com.cyancoder.aiorchestrator.repo.ProvisioningRunRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MongoProvisioningRunServiceTest {

    @Test
    void blocksProvisioningWhenFollowUpQuestionsRemain() {
        ClientAppDraftRepository draftRepository = mock(ClientAppDraftRepository.class);
        ProvisioningRunRepository runRepository = mock(ProvisioningRunRepository.class);
        PlatformProvisioningService platformProvisioningService = mock(PlatformProvisioningService.class);
        MongoProvisioningRunService service = new MongoProvisioningRunService(draftRepository, runRepository, platformProvisioningService);

        ClientAppDraft draft = new ClientAppDraft();
        draft.setDraftId("draft-1");
        draft.setTenantKey("tenant-1");
        draft.setSiteKey("site-1");
        draft.setPendingQuestionKeys(List.of("subdomainPrefix"));
        draft.setPendingQuestions(List.of("Which subdomain prefix should be used before a custom domain is connected?"));

        when(draftRepository.findByDraftId("draft-1")).thenReturn(Optional.of(draft));
        doAnswer(invocation -> {
            ProvisioningRun run = invocation.getArgument(0);
            run.setId("mongo-1");
            return run;
        }).when(runRepository).save(any(ProvisioningRun.class));

        ProvisioningRunDto result = service.provisionDraft("draft-1", new ProvisionDraftRequest("APPLY", "idem-1", "API", "tester"));

        assertEquals(ProvisioningRunStatus.BLOCKED, result.status());
        assertFalse(result.stepResults().isEmpty());
        assertEquals("BLOCKED", result.stepResults().get(0).getStatus());
        verifyNoInteractions(platformProvisioningService);
    }
}
