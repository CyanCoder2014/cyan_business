package com.cyancoder.buyer.repository;

import com.cyancoder.buyer.entity.BuyerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuyerRepository extends JpaRepository<BuyerEntity,String> {

    BuyerEntity findByBuyerId(String buyerId);


}
