package com.cyancoder.billing.repository;

import com.cyancoder.billing.model.TenantUsageCounterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

public interface TenantUsageCounterRepository extends JpaRepository<TenantUsageCounterEntity, TenantUsageCounterEntity.Key> {
    List<TenantUsageCounterEntity> findByIdTenantKey(String tenantKey);

    @Modifying
    @Transactional
    @Query(value = """
        insert into tenant_usage_counters (tenant_key, metric_key, counter_value, updated_at)
        values (:tenantKey, :metricKey, :delta, :now)
        on conflict (tenant_key, metric_key)
        do update set counter_value = tenant_usage_counters.counter_value + :delta, updated_at = :now
        """, nativeQuery = true)
    void increment(@Param("tenantKey") String tenantKey, @Param("metricKey") String metricKey, @Param("delta") long delta, @Param("now") Instant now);
}
