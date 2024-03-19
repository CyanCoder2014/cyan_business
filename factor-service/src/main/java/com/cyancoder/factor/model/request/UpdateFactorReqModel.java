package com.cyancoder.factor.model.request;

import com.cyancoder.factor.model.BuyerModel;
import com.cyancoder.factor.model.FactorItemModel;
import lombok.Data;

import java.util.List;

@Data
public class UpdateFactorReqModel {
    private String factorId;

    private String code;

    private String companyId;
    private BuyerModel buyer;

    private String factorDate;
    private String payType;
    private Double payed;
    private String state;
    private String note;
}
