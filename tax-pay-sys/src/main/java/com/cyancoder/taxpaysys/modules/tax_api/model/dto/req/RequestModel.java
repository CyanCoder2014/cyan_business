package com.cyancoder.taxpaysys.modules.tax_api.model.dto.req;


import com.cyancoder.taxpaysys.modules.tax_api.entity.general.SellerUser;
import com.cyancoder.taxpaysys.modules.tax_api.model.Header;
import com.cyancoder.taxpaysys.util.CryptoUtils;
import com.cyancoder.taxpaysys.util.KeyUtil;
import com.cyancoder.taxpaysys.util.SignText;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

@Data
@JsonFormat
public class RequestModel {

    public RequestModel(Header header, String packetType, DataModel data, SellerUser seller) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        this.time = 1;
        this.packet = PacketModel.builder()
                .uid(null)
                .packetType(packetType)
                .retry(false)
                .data(data)
                .encryptionKeyId("")
                .symmetricKey("")
                .iv("")
                .fiscalId("")
                .dataSignature("")
                .build();
        this.setSignature(header,seller);
    }

    private int time;
    private PacketModel packet;
    private String signature;

    protected void setSignature(Header header, SellerUser seller) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        String normalJsonStr = CryptoUtils.normalJson(packet, header);
        this.signature = SignText.getSignedText(normalJsonStr, "SHA256WITHRSA", KeyUtil.getPrivateKey(seller));

    }
}