package com.cyancoder.buyer.repository;

import com.cyancoder.buyer.entity.FactorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FactorRepository extends JpaRepository<FactorEntity,Integer> {

    FactorEntity findByFactorId(Integer factorId);

    FactorEntity findByFactorIdOrTitle(Integer factorId, String title);

}
