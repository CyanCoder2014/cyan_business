package com.cyancoder.factor.model;


import com.cyancoder.factor.entity.BuyerEntity;
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

    private Long id;


    public BuyerModel(BuyerEntity buyerEntity) {
        BeanUtils.copyProperties(buyerEntity, this);
    }
}
