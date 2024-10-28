package com.cyancoder.factor.model;

import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Builder(toBuilder = true)
public record FactorFilterModel(
        String codeFrom,
        String codeTo,
        String companyId,
        String buyerId,
        String state,
        String type,
        @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
        @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate
) {
}
