package com.cyancoder.factor.model;


import com.cyancoder.factor.entity.FactorEntity;
import com.cyancoder.factor.entity.FactorItemEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FactorItemModel {

    public FactorItemModel(FactorItemEntity factorItemEntity){
        BeanUtils.copyProperties(factorItemEntity, this);

        if (factorItemEntity.getProduct()!=null){
            ProductModel productModel = new ProductModel(factorItemEntity.getProduct());
            this.setProduct(productModel);
        }
    }



    private String factorItemId;
    private FactorModel factor;
    private ProductModel product;
    private Double amount;
    private Double price;
    private Double discount;
    private Double tax;
    private Double other_charge;
    private String detail;



    private String state;

}
