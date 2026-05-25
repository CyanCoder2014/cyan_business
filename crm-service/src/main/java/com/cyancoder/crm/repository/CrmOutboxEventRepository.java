package com.cyancoder.crm.repository;

import com.cyancoder.crm.entity.CrmOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrmOutboxEventRepository extends JpaRepository<CrmOutboxEvent, Long> {
    List<CrmOutboxEvent> findTop20ByStatusInOrderByCreatedAtAsc(List<String> statuses);
}
