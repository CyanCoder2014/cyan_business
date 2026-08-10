package com.cyancoder.billing.repository;
import com.cyancoder.billing.model.TenantSubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TenantSubscriptionRepository extends JpaRepository<TenantSubscriptionEntity, String> {}
