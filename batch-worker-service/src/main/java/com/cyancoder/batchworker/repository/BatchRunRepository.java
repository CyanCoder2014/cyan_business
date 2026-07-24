package com.cyancoder.batchworker.repository;

import com.cyancoder.batchworker.domain.BatchRun;
import com.cyancoder.batchworker.domain.BatchRunStatus;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

public interface BatchRunRepository extends JpaRepository<BatchRun, UUID> {
    Optional<BatchRun> findByTenantKeyAndSiteKeyAndDefinitionKeyAndRunKey(
            String tenantKey, String siteKey, String definitionKey, String runKey);

    List<BatchRun> findByTenantKeyAndSiteKeyOrderByCreatedAtDesc(
            String tenantKey, String siteKey, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from BatchRun r where r.status = :status order by r.createdAt")
    List<BatchRun> lockNextByStatus(BatchRunStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from BatchRun r where r.status = com.cyancoder.batchworker.domain.BatchRunStatus.RUNNING "
            + "and r.leaseUntil < :now order by r.leaseUntil")
    List<BatchRun> lockExpired(Instant now, Pageable pageable);

    @Modifying
    @Query("update BatchRun r set r.leaseUntil = :lease, r.heartbeatAt = :now "
            + "where r.id = :id and r.workerId = :worker and r.status = "
            + "com.cyancoder.batchworker.domain.BatchRunStatus.RUNNING")
    int renewLease(@Param("id") UUID id, @Param("worker") String worker,
            @Param("now") Instant now, @Param("lease") Instant lease);

    @Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("update BatchRun r set r.batchExecutionId = :executionId where r.id = :id")
    int recordBatchExecution(@Param("id") UUID id, @Param("executionId") long executionId);
}
