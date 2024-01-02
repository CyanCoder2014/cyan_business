package com.cyancoder.client.event;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientCreatedEvent {

    private final  String clientId;


    private String note;




}
