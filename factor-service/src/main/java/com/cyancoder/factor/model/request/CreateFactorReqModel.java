package com.cyancoder.factor.model.request;


import com.cyancoder.factor.model.BuyerModel;
import com.cyancoder.factor.model.FactorItemModel;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
public class CreateFactorReqModel {


    private String factorId;


    private String code;


    private List<FactorItemModel> items;

    private String companyId;
    private BuyerModel buyer;

    private String factorDate;
    private String payType;
    private Double payed;
    private String state;
    private String note;


}
