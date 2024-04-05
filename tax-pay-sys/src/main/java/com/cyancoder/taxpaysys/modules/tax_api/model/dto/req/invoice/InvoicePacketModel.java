package com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.invoice;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@JsonFormat
public class InvoicePacketModel {


    private String uid;
    private String packetType;
    private boolean retry;
    private byte[] data;
    private String encryptionKeyId;
    private String symmetricKey;
    private byte[] iv;
    private String fiscalId;
    private String dataSignature;





}
