package com.cyancoder.payment.repository;

import com.cyancoder.payment.domain.PaymentTransactionStatus;
import com.cyancoder.payment.entity.PaymentTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, Long> {
    Optional<PaymentTransactionEntity> findByTransactionKey(String transactionKey);
    List<PaymentTransactionEntity> findByStatusOrderByCreatedAtDesc(PaymentTransactionStatus status);
    List<PaymentTransactionEntity> findByOrderKeyOrderByCreatedAtDesc(String orderKey);
}
