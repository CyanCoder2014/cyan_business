package com.cyancoder.factor.command;


import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@Builder
public class CreateFactorCommand {


    @TargetAggregateIdentifier
    private final String factorId;


    private String code;
    private String note;



}
