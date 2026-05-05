package com.cyancoder.payment.repository;

import com.cyancoder.payment.entity.PaymentMethodEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethodEntity, Long> {
    Optional<PaymentMethodEntity> findByMethodKey(String methodKey);
    List<PaymentMethodEntity> findByEnabledTrueAndActiveTrueOrderByPriorityOrderAscDisplayNameAsc();
}
