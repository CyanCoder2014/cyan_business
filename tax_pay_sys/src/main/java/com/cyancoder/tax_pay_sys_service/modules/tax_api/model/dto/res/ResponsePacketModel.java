package com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.res;

import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.DataModel;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.res.auth.AuthDataModel;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat
public class ResponsePacketModel {

    public String uid;

    public DataModel data;
    public String packetType;
    public String encryptionKeyId;
    public String symmetricKey;
    public String iv;
}
