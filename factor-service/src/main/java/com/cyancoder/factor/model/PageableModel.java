package com.cyancoder.factor.model;

import lombok.Builder;

import java.util.Date;

@Builder(toBuilder = true)
public record PageableModel(

        SortOrder sortOrder,
        String sortKey,
        Integer page,
        Integer pageSize
) {

    public enum SortOrder{
        DESC,
        ASC
    }

}
