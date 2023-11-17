package com.cyancoder.taxpaysys.modules.content.dto;

import java.util.List;


public class SearchResultModel<E> {

    private List<E> result;
    private PaginationModel pagination;

    public List<E> getResult() {
        return result;
    }

    public void setResult(List<E> result) {
        this.result = result;
    }

    public PaginationModel getPagination() {
        return pagination;
    }

    public void setPagination(PaginationModel pagination) {
        this.pagination = pagination;
    }
}
