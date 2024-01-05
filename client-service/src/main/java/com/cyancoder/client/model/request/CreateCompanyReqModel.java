package com.cyancoder.client.model.request;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
@JsonFormat
public class CreateCompanyReqModel {

    private String companyId;

    private String clientId;
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
