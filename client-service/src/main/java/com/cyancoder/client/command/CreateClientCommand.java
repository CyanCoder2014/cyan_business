package com.cyancoder.client.command;


import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@Builder
public class CreateClientCommand {


    @TargetAggregateIdentifier
    private final String clientId;

    private String name;
    private Long nationalCode;
    private String economicCode;
    private String uniqueCode;
    private String pk;
    private String legalType;
    private String tell;
    private String address;
    private String postCode;
    private String cityId;




}
