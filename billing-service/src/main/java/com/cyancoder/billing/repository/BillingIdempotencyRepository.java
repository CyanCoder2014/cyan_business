package com.cyancoder.billing.repository;
import com.cyancoder.billing.model.BillingIdempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
public interface BillingIdempotencyRepository extends JpaRepository<BillingIdempotencyEntity, String> {}
