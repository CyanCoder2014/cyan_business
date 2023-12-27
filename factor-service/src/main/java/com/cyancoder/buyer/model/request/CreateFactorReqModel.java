package com.cyancoder.buyer.model.request;


import com.cyancoder.buyer.model.FactorItemModel;
import lombok.Data;

import java.util.List;

@Data
public class CreateFactorReqModel {


    private String factorId;


    private final String code;
    private final String note;


    private List<FactorItemModel> items;

}
