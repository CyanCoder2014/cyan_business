package com.cyancoder.product.model;


import com.cyancoder.product.entity.BuyerEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyerModel {

    private String buyerId;


    public BuyerModel(BuyerEntity buyerEntity) {
        BeanUtils.copyProperties(buyerEntity, this);
    }
}
