package com.cyancoder.buyer.event;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BuyerCreatedEvent {

    private final  Long buyerId;




}
