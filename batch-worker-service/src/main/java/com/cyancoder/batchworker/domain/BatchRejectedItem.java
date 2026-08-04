package com.cyancoder.batchworker.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "batch_rejected_items",
        indexes = @Index(name = "idx_batch_rejected_run", columnList = "run_id,created_at"))
public class BatchRejectedItem {
    @Id @GeneratedValue private UUID id;
    @Column(name = "run_id", nullable = false) private UUID runId;
    @Column(name = "payload_json", nullable = false, length = 8000) private String payloadJson;
    @Column(nullable = false, length = 2000) private String reason;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    public UUID getId() { return id; }
    public UUID getRunId() { return runId; }
    public void setRunId(UUID value) { runId = value; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String value) { payloadJson = value; }
    public String getReason() { return reason; }
    public void setReason(String value) { reason = value; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant value) { createdAt = value; }
}
