package com.cyancoder.financeautomation.repository;

import com.cyancoder.financeautomation.entity.FinanceAutomationAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FinanceAutomationActionRepository extends JpaRepository<FinanceAutomationAction, Long> {
    Optional<FinanceAutomationAction> findByEventKey(String eventKey);
}
