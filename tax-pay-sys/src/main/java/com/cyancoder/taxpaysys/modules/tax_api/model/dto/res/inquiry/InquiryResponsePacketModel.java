package com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.inquiry;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
@JsonFormat
public class InquiryResponsePacketModel {

    public String referenceNumber;
    public String uid;

    public InquiryDataModel data;
    public String packetType;
    public String fiscalId;
    public String sign;
    public String status;



}
