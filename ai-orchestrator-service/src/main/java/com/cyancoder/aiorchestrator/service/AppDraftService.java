package com.cyancoder.aiorchestrator.service;

import com.cyancoder.aiorchestrator.api.dto.CreateDraftRequest;
import com.cyancoder.aiorchestrator.api.dto.UpdateDraftRequest;
import com.cyancoder.aiorchestrator.domain.ClientAppDraft;

import java.util.List;
import java.util.Optional;

public interface AppDraftService {
    ClientAppDraft createDraft(CreateDraftRequest request, String createdBy);
    List<ClientAppDraft> listDrafts(String tenantKey, String siteKey, String clientKey);
    ClientAppDraft getDraft(String draftId);
    ClientAppDraft updateDraft(String draftId, UpdateDraftRequest request, String updatedBy);
    Optional<ClientAppDraft> resolveKnownAppDraft(String appType, String tenantKey, String siteKey, String clientKey, String prompt);
}
