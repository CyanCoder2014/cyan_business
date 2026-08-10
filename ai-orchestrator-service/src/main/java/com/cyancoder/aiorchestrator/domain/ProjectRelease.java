package com.cyancoder.aiorchestrator.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document("ai_project_releases")
public class ProjectRelease {
    @Id private String id;
    private String releaseId; private String draftId; private String tenantKey; private String siteKey;
    private Integer sourceRevision; private String provisioningRunId; private String status;
    private PlatformAppDslDefinition snapshot; private String createdBy; private Instant createdAt; private Instant activatedAt;
    public String getId(){return id;} public void setId(String v){id=v;} public String getReleaseId(){return releaseId;} public void setReleaseId(String v){releaseId=v;}
    public String getDraftId(){return draftId;} public void setDraftId(String v){draftId=v;} public String getTenantKey(){return tenantKey;} public void setTenantKey(String v){tenantKey=v;}
    public String getSiteKey(){return siteKey;} public void setSiteKey(String v){siteKey=v;} public Integer getSourceRevision(){return sourceRevision;} public void setSourceRevision(Integer v){sourceRevision=v;}
    public String getProvisioningRunId(){return provisioningRunId;} public void setProvisioningRunId(String v){provisioningRunId=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public PlatformAppDslDefinition getSnapshot(){return snapshot;} public void setSnapshot(PlatformAppDslDefinition v){snapshot=v;} public String getCreatedBy(){return createdBy;} public void setCreatedBy(String v){createdBy=v;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;} public Instant getActivatedAt(){return activatedAt;} public void setActivatedAt(Instant v){activatedAt=v;}
}
