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







    private Long id;//////////////////////////

    private String factorId;
    private String note;
    private BuyerModel buyer;
    private String buyerId;


    private String code;
    private String codeddd;


    private List<FactorItemModel> items;








//
////    private ProductTypeEntity productType;
//
//
//    private ProductModel product;
//
//
//    private String productName;
//
//
//    private long numbers;
//
//    private String unit;
//
//    private BigDecimal unitPrice;
//
//    private BigDecimal discount;
//
//    private BigDecimal finalPrice;
//
//    private Date factorDate;
//
//    private BigDecimal tax;
//
//    private BigDecimal weight;
//
//    private BigDecimal pricePlusTax;
//
//    private SellType sellType;
//
//
//    private PayState payState;
//
//    private String distribution;
//
//
////    private BuyerTypeEntity buyerType;////////////////////////// legal or not
//
//    private String fullName;
//
//    private String nationalCode;
//
//    private Person person;
//
//    private String registerNumber;
//
////    private CityEntity city;
//
//    private String address;
//
//    private String postCode;
//
//    private String tellNumber;
//
//    private String economicCode;
//
//    private Date createdOn;
//
//    private Date editedOn;
//
//
////    @Enumerated(EnumType.STRING)////////////////////
//    private State state;
//
////    @Enumerated(EnumType.STRING)//////////////////////////
//    private Status status;








}
