package com.cyancoder.client.command;


import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@Builder
public class CreateClientCommand {


    @TargetAggregateIdentifier
    private final String clientId;



}
