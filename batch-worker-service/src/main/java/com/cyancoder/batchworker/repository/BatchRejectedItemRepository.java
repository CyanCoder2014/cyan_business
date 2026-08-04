package com.cyancoder.batchworker.repository;

import com.cyancoder.batchworker.domain.BatchRejectedItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchRejectedItemRepository extends JpaRepository<BatchRejectedItem, UUID> {
    List<BatchRejectedItem> findAllByRunIdOrderByCreatedAtAsc(UUID runId);
}
