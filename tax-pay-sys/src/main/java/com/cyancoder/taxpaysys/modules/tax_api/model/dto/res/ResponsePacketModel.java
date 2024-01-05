package com.cyancoder.taxpaysys.modules.tax_api.model.dto.res;

import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.DataModel;
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
