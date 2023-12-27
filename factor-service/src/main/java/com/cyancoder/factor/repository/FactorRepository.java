package com.cyancoder.factor.repository;

import com.cyancoder.factor.entity.FactorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FactorRepository extends JpaRepository<FactorEntity,Long> {

    Optional<FactorEntity> findByFactorId(String factorId);

    FactorEntity findByFactorIdOrCode(String factorId, String code);

}
