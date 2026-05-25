package com.cyancoder.commerce.repository;

import com.cyancoder.commerce.entity.CommerceOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommerceOutboxEventRepository extends JpaRepository<CommerceOutboxEvent, Long> {
    List<CommerceOutboxEvent> findTop20ByStatusInOrderByCreatedAtAsc(List<String> statuses);
}
