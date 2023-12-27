package com.cyancoder.buyer.repository;

import com.cyancoder.buyer.entity.FactorItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FactorItemRepository extends JpaRepository<FactorItemEntity,String> {

    List<FactorItemEntity> findByFactorId(String factorId);

    Optional<FactorItemEntity> findByFactorItemId(String factorId);


}
