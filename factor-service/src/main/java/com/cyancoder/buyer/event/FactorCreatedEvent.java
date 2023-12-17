package com.cyancoder.buyer.event;


import lombok.Data;

@Data
public class FactorCreatedEvent {

    private final  Long factorId;


    private final  Long buyerId;


}
