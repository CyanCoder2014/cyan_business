package com.cyancoder.factor.model;


import com.cyancoder.factor.entity.FactorEntity;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class FactorModel {

    public FactorModel(FactorEntity factorEntity){
        BeanUtils.copyProperties(factorEntity, this);
        this.setItems(factorEntity.getItems().stream().map(FactorItemModel::new).collect(Collectors.toList()));




        BuyerModel buyerModel = new BuyerModel();
        if (factorEntity.getBuyerId()!=null){
            buyerModel.setBuyerId(factorEntity.getBuyerId());
            this.setBuyer(buyerModel);
        }

    }

    public FactorModel(FactorEntity factorEntity, String State){
        BeanUtils.copyProperties(factorEntity, this);
    }


    private String factorId;
    private String code;

    private List<FactorItemModel> items;

    private String companyId;
    private BuyerModel buyer;

    private Date factorDate;
    private String payType;
    private Double payed;
    private String state;
    private String note;

    private Date createdAt;




}
