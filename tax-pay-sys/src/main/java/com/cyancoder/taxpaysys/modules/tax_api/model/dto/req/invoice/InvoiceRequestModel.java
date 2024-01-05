package com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.invoice;


import com.cyancoder.taxpaysys.modules.tax_api.client.out_api.service.ServerInformationService;
import com.cyancoder.taxpaysys.modules.tax_api.entity.general.SellerUser;
import com.cyancoder.taxpaysys.modules.tax_api.model.Header;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.DataModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.server_info.ServerInfoPubKeyModel;
import com.cyancoder.taxpaysys.util.CryptoUtils;
import com.cyancoder.taxpaysys.util.Encryption;
import com.cyancoder.taxpaysys.util.KeyUtil;
import com.cyancoder.taxpaysys.util.SignText;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

@JsonFormat
@Data
public class InvoiceRequestModel  {

    private int time;
    private InvoicePacketModel packet;
    private String signature;


//    @Autowired
    ServerInformationService serverInformationService;

    public InvoiceRequestModel(Header header, String packetType, DataModel data, ServerInfoPubKeyModel serverKey, String privateKey) throws Exception {

        this.time = 1;

        String normalJsonStr = CryptoUtils.normalJson(data,null);

        SecretKey aesKey = Encryption.getAESKey(256);
        byte[] iv = Encryption.getRandomNonce(128);
        byte[] xor = Encryption.xor(normalJsonStr.getBytes(),iv);
        byte[] encrypt = Encryption.encrypt(xor,aesKey,iv);
        String encryptString = Encryption.encrypt(aesKey.getAlgorithm(),KeyUtil.getServerPublicKey(serverKey.key));



        this.packet = InvoicePacketModel.builder()
                .uid(header.getString("requestTraceId"))
                .packetType(packetType)
                .retry(false)
                .data(encrypt)

                .encryptionKeyId(serverKey.id)
                .symmetricKey(encryptString)
                .iv(iv)
                .fiscalId(null)

                .dataSignature("")

                .build();


        this.setDataSignature(data,privateKey);
        this.setSignature(header,privateKey);
    }


    protected void setSignature(Header header,String privateKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        String normalJsonStr = CryptoUtils.normalJson(packet, header);
        this.signature = SignText.getSignedText(normalJsonStr, "SHA256WITHRSA", KeyUtil.getPrivateKey(privateKey));

    }


    protected void setDataSignature(DataModel data,String privateKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        String normalJsonStr = CryptoUtils.normalJson(data, null);
        this.packet.setDataSignature(SignText.getSignedText(normalJsonStr, "SHA256WITHRSA", KeyUtil.getPrivateKey(privateKey)));

    }


}
