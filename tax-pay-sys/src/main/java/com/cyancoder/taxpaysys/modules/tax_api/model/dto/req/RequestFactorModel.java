package com.cyancoder.taxpaysys.modules.tax_api.model.dto.req;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonFormat
@NoArgsConstructor
public class RequestFactorModel {

    public RequestFactorModel(String companyId){
        this.companyId = companyId;

    }

    private String companyId;

    private String codeFrom;
    private String codeTo;

    private String fromDate;
    private String toDate;

    private String factorId;


}