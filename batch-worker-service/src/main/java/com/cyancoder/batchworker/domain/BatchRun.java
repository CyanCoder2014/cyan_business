package com.cyancoder.batchworker.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "batch_runs",
        uniqueConstraints = @UniqueConstraint(name = "uk_batch_run_scope_key",
                columnNames = {"tenant_key", "site_key", "definition_key", "run_key"}),
        indexes = {
                @Index(name = "idx_batch_run_dispatch", columnList = "status,lease_until,created_at"),
                @Index(name = "idx_batch_run_scope", columnList = "tenant_key,site_key,created_at")
        })
public class BatchRun {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_key", nullable = false, length = 120) private String tenantKey;
    @Column(name = "site_key", nullable = false, length = 120) private String siteKey;
    @Column(name = "definition_key", nullable = false, length = 160) private String definitionKey;
    @Column(name = "run_key", nullable = false, length = 200) private String runKey;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private BatchRunStatus status;
    @Column(name = "worker_id", length = 200) private String workerId;
    @Column(name = "lease_until") private Instant leaseUntil;
    @Column(name = "heartbeat_at") private Instant heartbeatAt;
    @Column(name = "batch_execution_id") private Long batchExecutionId;
    @Column(name = "read_count", nullable = false) private long readCount;
    @Column(name = "write_count", nullable = false) private long writeCount;
    @Column(name = "skip_count", nullable = false) private long skipCount;
    @Column(name = "error_message", length = 4000) private String errorMessage;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "started_at") private Instant startedAt;
    @Column(name = "completed_at") private Instant completedAt;
    @Version private long version;

    public UUID getId() { return id; }
    public String getTenantKey() { return tenantKey; }
    public void setTenantKey(String value) { tenantKey = value; }
    public String getSiteKey() { return siteKey; }
    public void setSiteKey(String value) { siteKey = value; }
    public String getDefinitionKey() { return definitionKey; }
    public void setDefinitionKey(String value) { definitionKey = value; }
    public String getRunKey() { return runKey; }
    public void setRunKey(String value) { runKey = value; }
    public BatchRunStatus getStatus() { return status; }
    public void setStatus(BatchRunStatus value) { status = value; }
    public String getWorkerId() { return workerId; }
    public void setWorkerId(String value) { workerId = value; }
    public Instant getLeaseUntil() { return leaseUntil; }
    public void setLeaseUntil(Instant value) { leaseUntil = value; }
    public Instant getHeartbeatAt() { return heartbeatAt; }
    public void setHeartbeatAt(Instant value) { heartbeatAt = value; }
    public Long getBatchExecutionId() { return batchExecutionId; }
    public void setBatchExecutionId(Long value) { batchExecutionId = value; }
    public long getReadCount() { return readCount; }
    public void setReadCount(long value) { readCount = value; }
    public long getWriteCount() { return writeCount; }
    public void setWriteCount(long value) { writeCount = value; }
    public long getSkipCount() { return skipCount; }
    public void setSkipCount(long value) { skipCount = value; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String value) { errorMessage = value; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant value) { createdAt = value; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant value) { startedAt = value; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant value) { completedAt = value; }
}
