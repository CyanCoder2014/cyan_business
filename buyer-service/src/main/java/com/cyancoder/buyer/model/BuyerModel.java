package com.cyancoder.buyer.model;


import com.cyancoder.buyer.entity.BuyerEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuyerModel {

    private Long id;


    public BuyerModel(BuyerEntity buyerEntity) {
        BeanUtils.copyProperties(buyerEntity, this);
    }
}
