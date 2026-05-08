package com.cyancoder.aiorchestrator.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document("ai_provisioning_runs")
public class ProvisioningRun {
    @Id
    private String id;
    private String runId;
    private String draftId;
    private String releaseId;
    private String tenantKey;
    private String siteKey;
    private ProvisioningRunStatus status;
    private List<ProvisioningStepResult> stepResults = new ArrayList<>();
    private String triggerType;
    private String triggeredBy;
    private Instant startedAt;
    private Instant finishedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }
    public String getDraftId() { return draftId; }
    public void setDraftId(String draftId) { this.draftId = draftId; }
    public String getReleaseId() { return releaseId; }
    public void setReleaseId(String releaseId) { this.releaseId = releaseId; }
    public String getTenantKey() { return tenantKey; }
    public void setTenantKey(String tenantKey) { this.tenantKey = tenantKey; }
    public String getSiteKey() { return siteKey; }
    public void setSiteKey(String siteKey) { this.siteKey = siteKey; }
    public ProvisioningRunStatus getStatus() { return status; }
    public void setStatus(ProvisioningRunStatus status) { this.status = status; }
    public List<ProvisioningStepResult> getStepResults() { return stepResults; }
    public void setStepResults(List<ProvisioningStepResult> stepResults) { this.stepResults = stepResults; }
    public String getTriggerType() { return triggerType; }
    public void setTriggerType(String triggerType) { this.triggerType = triggerType; }
    public String getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }
}
