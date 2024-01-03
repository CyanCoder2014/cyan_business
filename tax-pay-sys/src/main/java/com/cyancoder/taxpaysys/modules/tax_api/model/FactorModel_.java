package com.cyancoder.taxpaysys.modules.tax_api.model;


import com.cyancoder.taxpaysys.modules.tax_api.entity.general.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@JsonFormat
public class FactorModel_ {






    private Long id;//////////////////////////

    private String factorId;
    private String note;
    private Object buyer;
    private String buyerId;


    private String code;
    private String codeddd;


    private List<FactorItemModel> items;
//    private Object items;





}
