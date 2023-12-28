package com.cyancoder.factor.model;


import lombok.Data;

import java.util.List;

@Data
public class FactorModel {


    private String factorId;
    private String code;
    private String note;

    private List<FactorItemModel> items;

    private String buyerId;


}
