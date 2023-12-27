package com.cyancoder.factor.command;


import com.cyancoder.buyer.model.FactorItemModel;
import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.List;

@Data
@Builder
public class CreateFactorCommand {


    @TargetAggregateIdentifier
    private final String factorId;


    private String code;
    private String note;

    private List<FactorItemModel> items;



}
