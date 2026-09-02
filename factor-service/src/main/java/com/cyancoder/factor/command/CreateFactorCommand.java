package com.cyancoder.factor.command;


import com.cyancoder.factor.model.BuyerModel;
import com.cyancoder.factor.model.FactorItemModel;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class CreateFactorCommand {


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

    private String type;
    private String pattern;
    private String contractId;

}
