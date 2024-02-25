package com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.inquiry;

import lombok.Data;

@Data
public class InquiryResponsePacketModel {

    public String uid;

    public InquiryDataModel data;
    public String packetType;
    public String encryptionKeyId;
    public String symmetricKey;
    public String iv;
    public String status;

}
