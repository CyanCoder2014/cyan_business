package com.cyancoder.reportautomation.repository;

import com.cyancoder.reportautomation.entity.ReportAutomationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportAutomationRecordRepository extends JpaRepository<ReportAutomationRecord, Long> {
    Optional<ReportAutomationRecord> findByEventKey(String eventKey);
}
