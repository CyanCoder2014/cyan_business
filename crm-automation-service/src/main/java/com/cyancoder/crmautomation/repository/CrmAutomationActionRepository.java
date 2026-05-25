package com.cyancoder.crmautomation.repository;

import com.cyancoder.crmautomation.entity.CrmAutomationAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CrmAutomationActionRepository extends JpaRepository<CrmAutomationAction, Long> {
    Optional<CrmAutomationAction> findByEventKey(String eventKey);
}
