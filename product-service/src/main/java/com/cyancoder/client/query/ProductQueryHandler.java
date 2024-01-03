package com.cyancoder.client.query;


import com.cyancoder.client.entity.ProductEntity;
import com.cyancoder.client.repository.ProductRepository;
import com.cyancoder.generic.model.Product;
import com.cyancoder.generic.query.FetchProductQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductQueryHandler {

    private  final ProductRepository productRepository;

    @QueryHandler
    public List<Product> filterProduct(FilterProductQuery query){

        List<Product> products = new ArrayList<>();

        List<ProductEntity> storedProducts = productRepository.findAll();

        return products;

    }



    @QueryHandler
    public Product fetchProduct(FetchProductQuery query){

        log.info("query::::: {}",query);


        ProductEntity productEntity = productRepository.findByProductId(query.getProductId());
        log.info("productEntity::::: {}",query);

        return Product.builder()
                .productId(productEntity.getProductId())
                .build();

    }



}
