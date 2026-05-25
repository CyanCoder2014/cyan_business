package com.cyancoder.report.repository;

import com.cyancoder.report.entity.ReportDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportDefinitionRepository extends JpaRepository<ReportDefinition, Long> {
    Optional<ReportDefinition> findByReportKey(String reportKey);
}
