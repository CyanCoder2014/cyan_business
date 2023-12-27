package com.cyancoder.factor.event;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactorCreatedEvent {

    private  String factorId;


    private String code;
    private String note;


    private Long buyerId;



}
