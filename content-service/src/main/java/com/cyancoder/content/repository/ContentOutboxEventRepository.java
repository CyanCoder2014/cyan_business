package com.cyancoder.content.repository;

import com.cyancoder.content.entity.ContentOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentOutboxEventRepository extends JpaRepository<ContentOutboxEvent, Long> {
    List<ContentOutboxEvent> findTop20ByStatusInOrderByCreatedAtAsc(List<String> statuses);
}
