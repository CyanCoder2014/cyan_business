package com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.inquiry.inquiry_data.inquiry_by_uid;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@JsonFormat
public class InquiryByUidPacketModel {


    private String uid;
    private String packetType;
    private boolean retry;
    private InquiryByUidDataModel data;
    private String encryptionKeyId;
    private String symmetricKey;
    private String iv;
    private String fiscalId;
    private String dataSignature;





}
