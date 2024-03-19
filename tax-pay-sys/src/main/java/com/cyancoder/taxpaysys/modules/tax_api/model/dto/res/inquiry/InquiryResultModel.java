package com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.inquiry;

import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.ResultModel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
@JsonFormat
public class InquiryResultModel{

    public InquiryResponsePacketModel result;

    public String signature;
    public String signatureKeyId;
    public String timestamp;
    public String encryptionKeyId;
    public String symmetricKey;
    public String iv;


}
