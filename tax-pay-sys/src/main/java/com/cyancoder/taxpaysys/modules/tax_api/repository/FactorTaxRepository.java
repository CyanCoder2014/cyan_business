package com.cyancoder.taxpaysys.modules.tax_api.repository;

import com.cyancoder.taxpaysys.modules.tax_api.entity.FactorTaxEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface FactorTaxRepository extends JpaRepository<FactorTaxEntity, String> {

    List<FactorTaxEntity> findByCreatedAtBetween(Date startOn, Date endOn);

    List<FactorTaxEntity> findByFactorId(String factorId);

    Optional<FactorTaxEntity> findByTaxApiUid(String taxApiUid);
    Optional<FactorTaxEntity> findByTaxApiReference(String taxApiReference);

    List<FactorTaxEntity> findByFactorTaxId(String factorTaxId);















}
