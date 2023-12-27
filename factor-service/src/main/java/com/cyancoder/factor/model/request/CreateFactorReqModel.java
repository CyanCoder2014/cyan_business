package com.cyancoder.factor.model.request;


import lombok.Data;

@Data
public class CreateFactorReqModel {


    private String factorId;


    private final String code;
    private final String note;
}
