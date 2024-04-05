package com.cyancoder.taxpaysys.modules.tax_api.model;


import lombok.Data;

@Data
public class ProductTypeModel {

    private String productTypeId;
    private String code;
    private String name;
    private String note;
    private String state;

}
