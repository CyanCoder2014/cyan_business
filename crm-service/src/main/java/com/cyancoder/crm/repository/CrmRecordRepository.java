package com.cyancoder.crm.repository;

import com.cyancoder.crm.entity.CrmRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CrmRecordRepository extends JpaRepository<CrmRecord, Long> {
    Optional<CrmRecord> findByRecordKey(String recordKey);
    List<CrmRecord> findByRecordType(String recordType);
}
