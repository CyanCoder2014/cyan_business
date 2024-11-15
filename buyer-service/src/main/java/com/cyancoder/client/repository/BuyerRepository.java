package com.cyancoder.client.repository;

import com.cyancoder.client.entity.BuyerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuyerRepository extends JpaRepository<BuyerEntity,String> {

    BuyerEntity findByBuyerId(String buyerId);
    BuyerEntity findFirstByNationalCode(Long nationalCode);


}
