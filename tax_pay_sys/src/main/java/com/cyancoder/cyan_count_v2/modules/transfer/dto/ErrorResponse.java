package main.java.com.cyancoder.tax_pay_sys_service.modules.transfer.dto;

import java.util.List;


public class ErrorResponse extends SignedRequest {

    private Long timestamp;

    private List<ErrorModel> errors;

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public List<ErrorModel> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorModel> errors) {
        this.errors = errors;
    }
}
