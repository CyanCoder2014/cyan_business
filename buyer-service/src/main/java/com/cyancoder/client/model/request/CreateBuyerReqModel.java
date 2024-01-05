package com.cyancoder.client.model.request;


import lombok.Data;

@Data
public class CreateBuyerReqModel {


    private Long buyerId;

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
