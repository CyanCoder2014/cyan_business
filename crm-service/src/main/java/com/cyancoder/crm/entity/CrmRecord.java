package com.cyancoder.crm.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "crm_records")
@Data
public class CrmRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String recordKey;

    @Column(nullable = false)
    private String recordType;

    private String fullName;
    private String companyName;
    private String email;
    private String mobile;
    private String status;
    private String source;
    private String ownerUserId;

    @Column(length = 4000)
    private String notes;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
