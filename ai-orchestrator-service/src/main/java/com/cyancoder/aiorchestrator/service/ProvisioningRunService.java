package com.cyancoder.aiorchestrator.service;

import com.cyancoder.aiorchestrator.api.dto.ProvisionDraftRequest;
import com.cyancoder.aiorchestrator.api.dto.ProvisioningRunDto;

import java.util.List;

public interface ProvisioningRunService {
    ProvisioningRunDto provisionDraft(String draftId, ProvisionDraftRequest request);
    List<ProvisioningRunDto> listRuns(String draftId);
    ProvisioningRunDto getRun(String runId);
}
