package com.cyancoder.buyer.repository;

import com.cyancoder.buyer.BuyerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuyerRepository extends JpaRepository<BuyerEntity,Integer> {

    BuyerEntity findByBuyerId(Long factorId);

    BuyerEntity findByFactorIdOrTitle(Integer factorId, String title);

}
