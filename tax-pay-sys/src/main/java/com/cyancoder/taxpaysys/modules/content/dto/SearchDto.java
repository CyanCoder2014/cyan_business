package com.cyancoder.taxpaysys.modules.content.dto;


import java.util.ArrayList;
import java.util.List;

public class SearchDto {
    private List<FilterDto> filters = new ArrayList();
    private int page = 1;
    private int size = 10;
    private List<OrderByDto> orderBy = null;
    private boolean skipCount = false;

    public void addFilter(FilterDto filterDTO) {
        filters.add(filterDTO);
    }

    public List<FilterDto> getFilters() {
        return filters;
    }

    public void setFilters(List<FilterDto> filters) {
        this.filters = filters;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<OrderByDto> getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(List<OrderByDto> orderBy) {
        this.orderBy = orderBy;
    }

    public boolean isSkipCount() {
        return skipCount;
    }

    public void setSkipCount(boolean skipCount) {
        this.skipCount = skipCount;
    }
}
