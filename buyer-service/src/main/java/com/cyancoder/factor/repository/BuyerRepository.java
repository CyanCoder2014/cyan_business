package com.cyancoder.factor.repository;

import com.cyancoder.factor.entity.BuyerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuyerRepository extends JpaRepository<BuyerEntity,Integer> {

    BuyerEntity findByBuyerId(Long factorId);

    BuyerEntity findByFactorIdOrTitle(Integer factorId, String title);

}
