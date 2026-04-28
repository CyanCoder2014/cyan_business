package com.cyancoder.finance.repository;

import com.cyancoder.finance.entity.FinanceTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FinanceTransactionRepository extends JpaRepository<FinanceTransaction, Long> {
    Optional<FinanceTransaction> findByTransactionKey(String transactionKey);
    List<FinanceTransaction> findByTransactionType(String transactionType);
}
