package com.cyancoder.factor.repository;

import com.cyancoder.factor.entity.FactorEntity;
import com.cyancoder.factor.entity.FactorItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FactorItemRepository extends JpaRepository<FactorItemEntity,String> {

    List<FactorItemEntity> findByFactor(FactorEntity factor);

    Optional<FactorItemEntity> findByFactorItemId(String factorId);


}
