package com.cyancoder.factor.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonFormat
@NoArgsConstructor
@AllArgsConstructor
public class RequestTaxModel {
    public RequestTaxModel(String factorId) {
        this.companyId = " ";
        this.uniqueCode = " ";
        this.factorId = factorId;
    }

    private String uniqueCode;

    private String companyId;

    private String factorId;
}
