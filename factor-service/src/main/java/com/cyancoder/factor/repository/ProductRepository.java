package com.cyancoder.factor.repository;

import com.cyancoder.factor.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface ProductRepository extends JpaRepository<ProductEntity,String> {


    ProductEntity findByProductId(String product);

}
