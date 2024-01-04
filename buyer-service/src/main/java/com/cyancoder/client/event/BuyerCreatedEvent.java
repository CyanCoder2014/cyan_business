package com.cyancoder.client.event;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BuyerCreatedEvent {

    private final  String buyerId;


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
