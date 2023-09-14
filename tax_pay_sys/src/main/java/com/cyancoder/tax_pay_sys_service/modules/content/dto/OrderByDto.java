package com.cyancoder.tax_pay_sys_service.modules.content.dto;

public class OrderByDto {

    private String name;
    private boolean asc;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAsc() {
        return asc;
    }

    public void setAsc(boolean asc) {
        this.asc = asc;
    }
}
