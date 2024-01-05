package com.cyancoder.factor.model;


import lombok.Data;

@Data
public class BuyerModel {

    private String buyerId;

    private Long nationalCode;
    private String economicCode;
    private String buyerType;
    private String tell;
    private String address;
    private String postCode;
    private String cityId;
    private String note;
    private boolean addNew;


}
