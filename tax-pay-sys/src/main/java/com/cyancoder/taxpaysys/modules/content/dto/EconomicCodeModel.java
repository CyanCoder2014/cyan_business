package com.cyancoder.taxpaysys.modules.content.dto;


public class EconomicCodeModel {

    private String nameTrade;
    private String taxpayerStatus;
    private String taxpayerType;
    private String postalcodeTaxpayer;
    private String addressTaxpayer;

    public String getNameTrade() {
        return nameTrade;
    }

    public void setNameTrade(String nameTrade) {
        this.nameTrade = nameTrade;
    }

    public String getTaxpayerStatus() {
        return taxpayerStatus;
    }

    public void setTaxpayerStatus(String taxpayerStatus) {
        this.taxpayerStatus = taxpayerStatus;
    }

    public String getTaxpayerType() {
        return taxpayerType;
    }

    public void setTaxpayerType(String taxpayerType) {
        this.taxpayerType = taxpayerType;
    }

    public String getPostalcodeTaxpayer() {
        return postalcodeTaxpayer;
    }

    public void setPostalcodeTaxpayer(String postalcodeTaxpayer) {
        this.postalcodeTaxpayer = postalcodeTaxpayer;
    }

    public String getAddressTaxpayer() {
        return addressTaxpayer;
    }

    public void setAddressTaxpayer(String addressTaxpayer) {
        this.addressTaxpayer = addressTaxpayer;
    }

    @Override
    public String toString() {
        return "EconomicCodeModel{" +
                "nameTrade='" + nameTrade + '\'' +
                ", taxpayerStatus='" + taxpayerStatus + '\'' +
                ", taxpayerType='" + taxpayerType + '\'' +
                ", postalcodeTaxpayer='" + postalcodeTaxpayer + '\'' +
                ", addressTaxpayer='" + addressTaxpayer + '\'' +
                '}';
    }
}
