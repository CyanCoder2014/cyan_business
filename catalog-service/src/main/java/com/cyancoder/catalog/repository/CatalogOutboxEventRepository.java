package com.cyancoder.catalog.repository;

import com.cyancoder.catalog.entity.CatalogOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatalogOutboxEventRepository extends JpaRepository<CatalogOutboxEvent, Long> {
    List<CatalogOutboxEvent> findTop20ByStatusInOrderByCreatedAtAsc(List<String> statuses);
}
