package com.cyancoder.factor.command;


import com.cyancoder.factor.model.BuyerModel;
import com.cyancoder.factor.model.FactorItemModel;
import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class CreateFactorCommand {


    @TargetAggregateIdentifier
    private final String factorId;


    private String code;

    private List<FactorItemModel> items;

    private String companyId;
    private BuyerModel buyer;

    private Date factorDate;
    private String payType;
    private Double payed;
    private String state;
    private String note;



}
