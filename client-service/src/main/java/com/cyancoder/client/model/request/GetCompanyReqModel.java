package com.cyancoder.client.model.request;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
@JsonFormat
public class GetCompanyReqModel {

    private String companyId;
    private Long nationalCode;
    private String uniqueCode;

}
