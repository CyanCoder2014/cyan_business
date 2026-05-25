package com.cyancoder.inventoryautomation.repository;

import com.cyancoder.inventoryautomation.entity.InventoryAutomationAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryAutomationActionRepository extends JpaRepository<InventoryAutomationAction, Long> {
    Optional<InventoryAutomationAction> findByEventKey(String eventKey);
}
