package com.cyancoder.tax_pay_sys_service.modules.content.dto;


import java.math.BigDecimal;


public class TaxAmountDto {

    private String id;

    private String invoiceItemId;

    private BigDecimal exemptAmount;

    private String exemptionReason;

    private BigDecimal taxAmount;

    private BigDecimal taxAmountToDigit;

    private String taxTypeId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInvoiceItemId() {
        return invoiceItemId;
    }

    public void setInvoiceItemId(String invoiceItemId) {
        this.invoiceItemId = invoiceItemId;
    }

    public BigDecimal getExemptAmount() {
        return exemptAmount;
    }

    public void setExemptAmount(BigDecimal exemptAmount) {
        this.exemptAmount = exemptAmount;
    }

    public String getExemptionReason() {
        return exemptionReason;
    }

    public void setExemptionReason(String exemptionReason) {
        this.exemptionReason = exemptionReason;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTaxAmountToDigit() {
        return taxAmountToDigit;
    }

    public void setTaxAmountToDigit(BigDecimal taxAmountToDigit) {
        this.taxAmountToDigit = taxAmountToDigit;
    }

    public String getTaxTypeId() {
        return taxTypeId;
    }

    public void setTaxTypeId(String taxTypeId) {
        this.taxTypeId = taxTypeId;
    }
}
