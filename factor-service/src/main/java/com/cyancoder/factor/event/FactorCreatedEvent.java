package com.cyancoder.factor.event;


import com.cyancoder.factor.model.BuyerModel;
import com.cyancoder.factor.model.FactorItemModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactorCreatedEvent {

    private  String factorId;


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
