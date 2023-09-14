package com.cyancoder.tax_pay_sys_service.modules.transfer.dto;


public class ErrorModel {

    private String detail;

    private String errorCode;

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

}
