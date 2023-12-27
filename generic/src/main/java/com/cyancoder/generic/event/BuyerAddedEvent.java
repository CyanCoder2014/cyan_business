package com.cyancoder.generic.event;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BuyerAddedEvent {

    private final String buyerId;


}
