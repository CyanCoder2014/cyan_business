package com.cyancoder.taxpaysys.modules.tax_api.model.dto.req;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@JsonFormat
public class PacketModel {


    private String uid;
    private String packetType;
    private boolean retry;
    private DataModel data;
    private String encryptionKeyId;
    private String symmetricKey;
    private String iv;
    private String fiscalId;
    private String dataSignature;





}
