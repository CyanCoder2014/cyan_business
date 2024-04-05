package com.cyancoder.client.rest;


import com.cyancoder.client.model.ProductModel;
import com.cyancoder.client.model.request.CreateProductReqModel;
import com.cyancoder.client.query.FilterProductQuery;
import lombok.RequiredArgsConstructor;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v2/api/product-service/products")
@RequiredArgsConstructor
public class ProductQueryController {


    private  final Environment env;
    private  final QueryGateway queryGateway;


    @GetMapping
    public List<ProductModel> createProduct(@RequestBody CreateProductReqModel createProductReqModel){

        FilterProductQuery filterProductQuery = new FilterProductQuery();

        List<ProductModel> products = queryGateway.query(filterProductQuery,
                ResponseTypes.multipleInstancesOf(ProductModel.class)).join();


        return products;


    }


}
