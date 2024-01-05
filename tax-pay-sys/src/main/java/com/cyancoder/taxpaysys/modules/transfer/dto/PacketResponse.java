package com.cyancoder.taxpaysys.modules.transfer.dto;


public class PacketResponse {

    private String uid;

    private String referenceNumber;

    private String errorCode;

    private String errorDetail;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDetail() {
        return errorDetail;
    }

    public void setErrorDetail(String errorDetail) {
        this.errorDetail = errorDetail;
    }

    @Override
    public String toString() {
        return "PacketResponse{" +
                "uid='" + uid + '\'' +
                ", referenceNumber='" + referenceNumber + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", errorDetail='" + errorDetail + '\'' +
                '}';
    }
}
