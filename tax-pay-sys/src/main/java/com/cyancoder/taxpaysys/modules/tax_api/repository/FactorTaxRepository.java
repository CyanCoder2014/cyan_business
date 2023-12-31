package com.cyancoder.taxpaysys.modules.tax_api.repository;

import com.cyancoder.taxpaysys.modules.tax_api.entity.FactorTaxEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface FactorTaxRepository extends JpaRepository<FactorTaxEntity, String> {

    List<FactorTaxEntity> findByCreatedOnBetween(Date startOn, Date endOn);


    List<FactorTaxEntity> findByTaxApiUid(String taxApiUid);

    List<FactorTaxEntity> findByFactorTaxId(String factorTaxId);















}
