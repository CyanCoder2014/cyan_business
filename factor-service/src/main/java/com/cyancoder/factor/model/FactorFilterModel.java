package com.cyancoder.factor.model;

import lombok.Builder;

import java.util.Date;

@Builder(toBuilder = true)
public record FactorFilterModel(
        String codeFrom,
        String codeTo,
        String companyId,
        String buyerId,
        String state,
        String type,
        Date startDate,
        Date endDate
) {
}
