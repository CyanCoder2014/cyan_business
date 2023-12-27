package com.cyancoder.buyer.event;


import com.cyancoder.buyer.model.FactorItemModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactorCreatedEvent {

    private  String factorId;


    private String code;
    private String note;

    private List<FactorItemModel> items;

    private Long buyerId;



}
