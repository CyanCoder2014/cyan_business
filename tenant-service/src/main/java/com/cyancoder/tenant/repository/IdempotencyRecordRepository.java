package com.cyancoder.tenant.repository;

import com.cyancoder.tenant.model.IdempotencyRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecordEntity, String> {}
