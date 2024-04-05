package com.cyancoder.taxpaysys.modules.content.dto;

public class PaginationModel {
    private int size;
    private int page;
    private long total;

    public PaginationModel() {
    }

    public PaginationModel(int size, int page, Long total) {
        this.size = size;
        this.page = page;
        if (total == null) {
            this.total = 0L;
        } else {
            this.total = total;
        }

    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getPage() {
        return this.page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public long getTotal() {
        return this.total;
    }

    public void setTotal(Long total) {
        if (total == null) {
            this.total = 0L;
        } else {
            this.total = total;
        }

    }
}
