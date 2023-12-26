package com.cyancoder.factor.event;


import lombok.Data;

@Data
public class FactorCreatedEvent {

    private final  Long factorId;


    private final  Long buyerId;


}
