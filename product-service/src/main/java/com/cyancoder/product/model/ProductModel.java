package com.cyancoder.product.model;


import com.cyancoder.product.entity.ProductEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductModel {

    private String productId;


    public ProductModel(ProductEntity productEntity) {
        BeanUtils.copyProperties(productEntity, this);
    }
}
