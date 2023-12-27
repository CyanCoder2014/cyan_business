package com.cyancoder.generic.command.buyer;


import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@Builder
public class AddOrEditBuyerCommand {


    @TargetAggregateIdentifier
    private final String buyerId;


}
