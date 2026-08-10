package com.cyancoder.media.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "media_upload")
public class MediaUploadEntity {
    @Id private String uploadId;
    @Column(nullable = false, unique = true) private String assetKey;
    @Column(nullable = false) private String tenantKey;
    private String siteKey;
    @Column(nullable = false) private String originalFileName;
    @Column(nullable = false) private String mimeType;
    @Column(nullable = false) private String visibility;
    @Column(nullable = false) private long expectedSizeBytes;
    private long uploadedSizeBytes;
    @Column(nullable = false) private String status;
    @Column(nullable = false) private String createdBy;
    @Column(nullable = false) private Instant createdAt;
    @Column(nullable = false) private Instant expiresAt;
    private Instant completedAt;
    private String storagePath;
    public String getUploadId() { return uploadId; } public void setUploadId(String value) { uploadId = value; }
    public String getAssetKey() { return assetKey; } public void setAssetKey(String value) { assetKey = value; }
    public String getTenantKey() { return tenantKey; } public void setTenantKey(String value) { tenantKey = value; }
    public String getSiteKey() { return siteKey; } public void setSiteKey(String value) { siteKey = value; }
    public String getOriginalFileName() { return originalFileName; } public void setOriginalFileName(String value) { originalFileName = value; }
    public String getMimeType() { return mimeType; } public void setMimeType(String value) { mimeType = value; }
    public String getVisibility() { return visibility; } public void setVisibility(String value) { visibility = value; }
    public long getExpectedSizeBytes() { return expectedSizeBytes; } public void setExpectedSizeBytes(long value) { expectedSizeBytes = value; }
    public long getUploadedSizeBytes() { return uploadedSizeBytes; } public void setUploadedSizeBytes(long value) { uploadedSizeBytes = value; }
    public String getStatus() { return status; } public void setStatus(String value) { status = value; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String value) { createdBy = value; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant value) { createdAt = value; }
    public Instant getExpiresAt() { return expiresAt; } public void setExpiresAt(Instant value) { expiresAt = value; }
    public Instant getCompletedAt() { return completedAt; } public void setCompletedAt(Instant value) { completedAt = value; }
    public String getStoragePath() { return storagePath; } public void setStoragePath(String value) { storagePath = value; }
}
