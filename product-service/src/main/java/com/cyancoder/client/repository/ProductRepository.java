package com.cyancoder.client.repository;

import com.cyancoder.client.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity,String> {

    ProductEntity findByProductId(String productId);


}
