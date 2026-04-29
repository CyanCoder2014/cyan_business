package com.cyancoder.finance.repository;

import com.cyancoder.finance.entity.FinanceOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinanceOutboxEventRepository extends JpaRepository<FinanceOutboxEvent, Long> {
    List<FinanceOutboxEvent> findTop20ByStatusInOrderByCreatedAtAsc(List<String> statuses);
}
