package com.cyancoder.taxpaysys.modules.content.dto;

import java.util.List;


public class InquiryByReferenceNumberDto {

    private List<String> referenceNumber;

    public List<String> getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(List<String> referenceNumber) {
        this.referenceNumber = referenceNumber;
    }
}