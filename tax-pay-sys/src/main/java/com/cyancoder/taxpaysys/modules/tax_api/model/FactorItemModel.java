package com.cyancoder.taxpaysys.modules.tax_api.model;


import com.cyancoder.taxpaysys.modules.tax_api.entity.general.*;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class FactorItemModel {


    private String factorItemId;

    private FactorModel factor;////////////////////

    private ProductModel product;

    private BigDecimal amount;

    private String unit;//////

    private BigDecimal price;


    private BigDecimal discount;

    private BigDecimal tax;

    private BigDecimal other_charge;

    private String detail;


    private String state;

}
