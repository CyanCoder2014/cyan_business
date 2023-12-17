package com.cyancoder.buyer.command;


import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@Builder
public class CreateFactorCommand {


    @TargetAggregateIdentifier
    private final Long factorId;



}
