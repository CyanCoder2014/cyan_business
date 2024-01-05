package com.cyancoder.client.event;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanyCreatedEvent {

    private String companyId;

    private  String clientId;
    private String name;
    private Long nationalCode;
    private String economicCode;
    private String uniqueCode;
    private String pk;
    private String legalType;
    private String tell;
    private String address;
    private String postCode;
    private String cityId;




}
