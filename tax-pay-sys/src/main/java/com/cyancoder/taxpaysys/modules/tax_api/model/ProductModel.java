package com.cyancoder.taxpaysys.modules.tax_api.model;


import lombok.Data;

@Data
public class ProductModel {

    private String productId;

    private ProductTypeModel productType;
    private UnitModel unit;
    private String code;
    private String name;
    private String note;
    private String state;

}
