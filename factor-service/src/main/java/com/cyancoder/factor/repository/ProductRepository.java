package com.cyancoder.factor.repository;

import com.cyancoder.factor.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ProductRepository extends JpaRepository<ProductEntity,String> {



}
