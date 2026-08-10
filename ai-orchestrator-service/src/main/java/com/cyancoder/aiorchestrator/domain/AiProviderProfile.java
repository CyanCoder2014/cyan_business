package com.cyancoder.aiorchestrator.domain;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("ai_provider_profiles")
@CompoundIndex(name="ai_provider_profile_scope_key", def="{'tenantKey':1,'siteKey':1,'profileKey':1}", unique=true)
public class AiProviderProfile {
    @Id private String id;
    @Version private Long revision;
    private String tenantKey;
    private String siteKey;
    private String profileKey;
    private String displayName;
    private String baseUrl;
    private String operationPath;
    private String model;
    private String secretRef;
    private Set<String> modalities = new LinkedHashSet<>();
    private boolean enabled = true;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;
    public String getId(){return id;} public void setId(String v){id=v;} public Long getRevision(){return revision;} public void setRevision(Long v){revision=v;}
    public String getTenantKey(){return tenantKey;} public void setTenantKey(String v){tenantKey=v;} public String getSiteKey(){return siteKey;} public void setSiteKey(String v){siteKey=v;}
    public String getProfileKey(){return profileKey;} public void setProfileKey(String v){profileKey=v;} public String getDisplayName(){return displayName;} public void setDisplayName(String v){displayName=v;}
    public String getBaseUrl(){return baseUrl;} public void setBaseUrl(String v){baseUrl=v;} public String getOperationPath(){return operationPath;} public void setOperationPath(String v){operationPath=v;}
    public String getModel(){return model;} public void setModel(String v){model=v;} public String getSecretRef(){return secretRef;} public void setSecretRef(String v){secretRef=v;}
    public Set<String> getModalities(){return modalities;} public void setModalities(Set<String> v){modalities=v==null?new LinkedHashSet<>():new LinkedHashSet<>(v);}
    public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;} public String getCreatedBy(){return createdBy;} public void setCreatedBy(String v){createdBy=v;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;} public Instant getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Instant v){updatedAt=v;}
}
