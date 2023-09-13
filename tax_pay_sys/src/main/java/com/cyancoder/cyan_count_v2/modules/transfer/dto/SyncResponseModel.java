package main.java.com.cyancoder.tax_pay_sys_service.modules.transfer.dto;


import java.util.List;

public class SyncResponseModel extends SignedRequest {

    private Long timestamp;

    private ResponsePacketModel result;

    private List<ErrorModel> errors;

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public ResponsePacketModel getResult() {
        return result;
    }

    public void setResult(ResponsePacketModel result) {
        this.result = result;
    }

    public List<ErrorModel> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorModel> errors) {
        this.errors = errors;
    }
}
