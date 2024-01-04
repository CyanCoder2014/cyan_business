package com.cyancoder.factor.query;

import lombok.Data;

@Data
public class FilterFactorQuery {

    private String companyId;

    private String codeFrom;
    private String codeTo;

    private String fromDate;
    private String toDate;

    private String factorId;

}
