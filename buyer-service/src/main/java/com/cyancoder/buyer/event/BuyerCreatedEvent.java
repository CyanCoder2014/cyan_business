package com.cyancoder.buyer.event;


import lombok.Data;

@Data
public class BuyerCreatedEvent {

    private final  Long factorId;


    private final  Long buyerId;


}
