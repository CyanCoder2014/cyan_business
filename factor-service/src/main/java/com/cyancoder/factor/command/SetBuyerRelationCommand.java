package com.cyancoder.factor.command;


import com.cyancoder.factor.model.BuyerModel;
import com.cyancoder.factor.model.FactorItemModel;
import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.List;

@Data
@Builder
public class SetBuyerRelationCommand {////////////////////////////////// temp


    @TargetAggregateIdentifier
    private final String factorId;


    private String code;
    private String note;

    private List<FactorItemModel> items;

    private BuyerModel buyer;


}
