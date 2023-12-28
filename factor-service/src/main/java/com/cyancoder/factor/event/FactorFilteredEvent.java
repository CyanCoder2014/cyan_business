package com.cyancoder.factor.event;


import com.cyancoder.factor.model.FactorItemModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactorFilteredEvent {

    private  String factorId;


    private String code;
    private String note;

    private List<FactorItemModel> items;

    private String buyerId;



}
