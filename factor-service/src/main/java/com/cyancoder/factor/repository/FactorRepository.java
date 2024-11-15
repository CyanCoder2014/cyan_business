package com.cyancoder.factor.repository;

import com.cyancoder.factor.entity.FactorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface FactorRepository extends JpaRepository<FactorEntity,String> {

    List<FactorEntity> findByCompanyIdAndFactorId(String CompanyId, String factorId);

    List<FactorEntity> findByCompanyIdAndCreatedAtBetween(String CompanyId, Date startOn, Date endOn);

    List<FactorEntity> findByCompanyIdAndCodeBetween(String CompanyId, String codeFrom, String codeTo);

    List<FactorEntity> findByCompanyId(String CompanyId);

    FactorEntity findByCompanyIdAndFactorIdOrCode(String CompanyId, String factorId, String code);
    FactorEntity findTopByCompanyIdOrderByFactorIdDesc(String CompanyId);
    FactorEntity findTopByCompanyIdOrderByCodeDesc(String CompanyId);


}
