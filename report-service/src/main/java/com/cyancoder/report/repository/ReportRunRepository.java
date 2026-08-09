package com.cyancoder.report.repository;
import com.cyancoder.report.entity.ReportRunEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ReportRunRepository extends JpaRepository<ReportRunEntity,String>{
 Optional<ReportRunEntity> findByTenantKeyAndIdempotencyKey(String tenantKey,String idempotencyKey);
 Optional<ReportRunEntity> findByRunIdAndTenantKeyAndSiteKey(String runId,String tenantKey,String siteKey);
 Page<ReportRunEntity> findByTenantKeyAndSiteKeyAndReportKey(String tenantKey,String siteKey,String reportKey,Pageable pageable);
}
