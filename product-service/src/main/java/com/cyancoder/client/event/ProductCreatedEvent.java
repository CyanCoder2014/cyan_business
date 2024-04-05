package com.cyancoder.client.event;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductCreatedEvent {

    private final  String productId;


    private String note;




}
