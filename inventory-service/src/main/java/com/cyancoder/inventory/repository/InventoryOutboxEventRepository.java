package com.cyancoder.inventory.repository;

import com.cyancoder.inventory.entity.InventoryOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryOutboxEventRepository extends JpaRepository<InventoryOutboxEvent, Long> {
    List<InventoryOutboxEvent> findTop20ByStatusInOrderByCreatedAtAsc(List<String> statuses);
}
