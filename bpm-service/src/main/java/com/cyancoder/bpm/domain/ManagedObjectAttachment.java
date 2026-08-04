package com.cyancoder.bpm.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Document("bpm_managed_object_attachments")
@CompoundIndex(name = "bpm_attachment_object_created", def = "{'objectId':1,'createdAt':1}")
public class ManagedObjectAttachment {
    @Id private String id;
    private String objectId;
    private String stateId;
    private String authorUserId;
    private String assetKey;
    private String fileName;
    private String downloadUrl;
    private String contentType;
    private Long sizeBytes;
    private Set<String> visibleToUserIds = new LinkedHashSet<>();
    private Set<String> visibleToRoles = new LinkedHashSet<>();
    private Set<String> visibleToGroups = new LinkedHashSet<>();
    private String visibleUntilState;
    private Map<String, Object> metadata = new LinkedHashMap<>();
    private Instant createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getObjectId() { return objectId; }
    public void setObjectId(String objectId) { this.objectId = objectId; }
    public String getStateId() { return stateId; }
    public void setStateId(String stateId) { this.stateId = stateId; }
    public String getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(String authorUserId) { this.authorUserId = authorUserId; }
    public String getAssetKey() { return assetKey; }
    public void setAssetKey(String assetKey) { this.assetKey = assetKey; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
    public Set<String> getVisibleToUserIds() { return visibleToUserIds; }
    public void setVisibleToUserIds(Set<String> values) { this.visibleToUserIds = safeSet(values); }
    public Set<String> getVisibleToRoles() { return visibleToRoles; }
    public void setVisibleToRoles(Set<String> values) { this.visibleToRoles = safeSet(values); }
    public Set<String> getVisibleToGroups() { return visibleToGroups; }
    public void setVisibleToGroups(Set<String> values) { this.visibleToGroups = safeSet(values); }
    public String getVisibleUntilState() { return visibleUntilState; }
    public void setVisibleUntilState(String visibleUntilState) { this.visibleUntilState = visibleUntilState; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata == null ? new LinkedHashMap<>() : new LinkedHashMap<>(metadata); }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    private static Set<String> safeSet(Set<String> values) { return values == null ? new LinkedHashSet<>() : new LinkedHashSet<>(values); }
}
