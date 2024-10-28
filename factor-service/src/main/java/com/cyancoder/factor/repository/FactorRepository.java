package com.cyancoder.factor.repository;

import com.cyancoder.factor.entity.FactorEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface FactorRepository extends JpaRepository<FactorEntity, String> {

    List<FactorEntity> findByCompanyIdAndFactorId(String CompanyId, String factorId);

    List<FactorEntity> findByCompanyIdAndCreatedAtBetween(String CompanyId, Date startOn, Date endOn);

    List<FactorEntity> findByCompanyIdAndCodeBetween(String CompanyId, String codeFrom, String codeTo);

    List<FactorEntity> findByCompanyId(String CompanyId);

    FactorEntity findByCompanyIdAndFactorIdOrCode(String CompanyId, String factorId, String code);

    FactorEntity findByFactorId(String factorId);

    FactorEntity findTopByCompanyIdOrderByFactorIdDesc(String CompanyId);

    FactorEntity findTopByCompanyIdOrderByCodeDesc(String CompanyId);

    @Transactional
    void deleteAllByCode(String code);


    Page<FactorEntity> findByCompanyIdAndCreatedAtBetweenAndCodeBetween(String CompanyId,
                                                                        Date startOn, Date endOn,
                                                                        String codeFrom, String codeTo,
                                                                        Pageable pageable);


    Page<FactorEntity> findByCompanyIdAndCreatedAtBetween(String CompanyId, Date startOn, Date endOn,
                                                          Pageable pageable);

    Page<FactorEntity> findByCompanyIdAndCodeBetween(String CompanyId, String codeFrom, String codeTo,
                                                     Pageable pageable);

}
