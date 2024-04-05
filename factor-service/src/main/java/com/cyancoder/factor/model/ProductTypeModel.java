package com.cyancoder.factor.model;


import com.cyancoder.factor.entity.UnitEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductTypeModel {

    private String productTypeId;
    private String code;
    private String name;
    private String note;
    private String state;

}
