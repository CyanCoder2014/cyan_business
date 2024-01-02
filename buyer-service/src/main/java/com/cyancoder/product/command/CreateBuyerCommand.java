package com.cyancoder.product.command;


import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@Builder
public class CreateBuyerCommand {


    @TargetAggregateIdentifier
    private final String buyerId;



}
