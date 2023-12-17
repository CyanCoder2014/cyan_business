package com.cyancoder.factor.repository;

import com.cyancoder.factor.entity.FactorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FactorRepository extends JpaRepository<FactorEntity,Integer> {

    FactorEntity findByFactorId(Integer factorId);

    FactorEntity findByFactorIdOrTitle(Integer factorId, String title);

}
