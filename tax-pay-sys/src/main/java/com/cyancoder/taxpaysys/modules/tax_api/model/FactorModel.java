package com.cyancoder.taxpaysys.modules.tax_api.model;


import com.cyancoder.taxpaysys.modules.tax_api.entity.general.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@JsonFormat
public class FactorModel {

    private String factorId;
    private String code;

    private List<FactorItemModel> items;

    private String companyId;
    private BuyerModel buyer;

    private Date factorDate;
    private String payType;
    private Double payed;
    private String state;
    private String note;
    private Date createdAt;

    private String type;
    private String pattern;
    private String contractId;

}
