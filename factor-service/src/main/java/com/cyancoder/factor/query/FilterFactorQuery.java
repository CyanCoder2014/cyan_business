package com.cyancoder.factor.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonFormat
public class FilterFactorQuery {

    private String companyId;

    private String codeFrom;
    private String codeTo;

    private String fromDate;
    private String toDate;

    private String factorId;

}
