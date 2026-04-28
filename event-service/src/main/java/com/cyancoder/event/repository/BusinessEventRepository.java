package com.cyancoder.event.repository;

import com.cyancoder.event.entity.BusinessEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessEventRepository extends JpaRepository<BusinessEvent, Long> {
    Optional<BusinessEvent> findByEventKey(String eventKey);
    List<BusinessEvent> findBySourceServiceOrderByOccurredAtDesc(String sourceService);
    List<BusinessEvent> findByEntityTypeOrderByOccurredAtDesc(String entityType);
    List<BusinessEvent> findByEntityKeyOrderByOccurredAtDesc(String entityKey);
    List<BusinessEvent> findByActionTypeOrderByOccurredAtDesc(String actionType);
}
