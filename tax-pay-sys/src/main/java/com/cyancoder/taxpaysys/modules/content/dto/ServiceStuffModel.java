package com.cyancoder.taxpaysys.modules.content.dto;

import java.math.BigDecimal;

public class ServiceStuffModel {

    private BigDecimal tax;

    private String itemId;

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    @Override
    public String toString() {
        return "ServiceStuffModel{" +
                "tax=" + tax +
                ", itemId='" + itemId + '\'' +
                '}';
    }
}
