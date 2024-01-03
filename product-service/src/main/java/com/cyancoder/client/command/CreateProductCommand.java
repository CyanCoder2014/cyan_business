package com.cyancoder.client.command;


import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@Builder
public class CreateProductCommand {


    @TargetAggregateIdentifier
    private final String productId;



}
