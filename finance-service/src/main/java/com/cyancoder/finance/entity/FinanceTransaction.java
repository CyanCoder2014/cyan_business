package com.cyancoder.finance.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "finance_transactions")
@Data
public class FinanceTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String transactionKey;

    @Column(nullable = false)
    private String transactionType;

    private String referenceType;
    private String referenceKey;
    private String accountKey;
    private String currency;
    private BigDecimal amount;
    private String status;

    @Column(length = 2000)
    private String description;

    @CreationTimestamp
    private Instant createdAt;
}
