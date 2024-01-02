package com.cyancoder.client.event;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BuyerCreatedEvent {

    private final  String buyerId;


    private String note;




}
