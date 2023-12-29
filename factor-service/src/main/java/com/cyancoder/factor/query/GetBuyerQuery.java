package com.cyancoder.factor.query;

import com.cyancoder.factor.model.FactorItemModel;
import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.List;

@Data
@Builder
public class GetBuyerQuery {


    private final String factorId;


    private String code;
    private String note;

    private List<FactorItemModel> items;

    private String buyerId;


}