package com.cyancoder.buyer.command;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateFactorCommand {


    @TargetAggregateIdentifier
    private final String factorId;



}
