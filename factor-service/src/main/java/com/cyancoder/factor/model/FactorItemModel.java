package com.cyancoder.factor.model;


import lombok.Data;

@Data
public class FactorItemModel {


    private String factorItemId;
    private String factorId;
    private String productId;
    private Double amount;
    private String unitId;
    private Double price;
    private Double discount;
    private Double tax;
    private Double other_charge;
    private String detail;



    private String state;

}
