package com.cyancoder.generic.command.buyer;


import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@Builder
public class AddOrEditBuyerCommand {


    @TargetAggregateIdentifier
    private final String buyerId;

    private Long nationalCode;
    private String economicCode;
    private String buyerType;
    private String tell;
    private String address;
    private String postCode;
    private String cityId;
    private String note;
    private boolean addNew;




}
