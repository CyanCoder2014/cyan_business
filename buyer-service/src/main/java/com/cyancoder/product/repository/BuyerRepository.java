package com.cyancoder.product.repository;

import com.cyancoder.product.entity.BuyerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuyerRepository extends JpaRepository<BuyerEntity,String> {

    BuyerEntity findByBuyerId(String buyerId);


}
