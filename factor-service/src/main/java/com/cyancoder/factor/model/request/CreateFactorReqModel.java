package com.cyancoder.factor.model.request;


import com.cyancoder.factor.model.FactorItemModel;
import lombok.Data;

import java.util.List;

@Data
public class CreateFactorReqModel {


    private String factorId;


    private final String code;
    private final String note;


    private List<FactorItemModel> items;

}
