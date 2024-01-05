package com.cyancoder.factor.model;


import com.cyancoder.factor.entity.FactorEntity;
import com.cyancoder.factor.entity.ProductEntity;
import com.cyancoder.factor.entity.ProductTypeEntity;
import com.cyancoder.factor.entity.UnitEntity;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductModel {

    public ProductModel(ProductEntity productEntity){
        BeanUtils.copyProperties(productEntity, this);
        UnitModel unitModel = new UnitModel(productEntity.getUnit());
        this.setUnit(unitModel);
    }

    private String productId;

    private ProductTypeModel productType;
    private UnitModel unit;
    private String code;
    private String name;
    private String note;
    private String state;


}
