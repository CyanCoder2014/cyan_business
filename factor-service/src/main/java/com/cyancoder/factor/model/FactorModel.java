package com.cyancoder.factor.model;


import com.cyancoder.factor.entity.FactorEntity;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class FactorModel {

    public FactorModel(FactorEntity factorEntity){
        BeanUtils.copyProperties(factorEntity, this);
        this.setItems(factorEntity.getItems().stream().map(FactorItemModel::new).collect(Collectors.toList()));
    }


    private String factorId;
    private String code;
    private String note;

    private List<FactorItemModel> items;

    private String buyerId;
    private BuyerModel buyer;



}
