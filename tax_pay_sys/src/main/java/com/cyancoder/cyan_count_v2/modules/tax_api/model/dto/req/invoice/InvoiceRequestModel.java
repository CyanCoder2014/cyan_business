package main.java.com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.invoice;


import com.cyancoder.tax_pay_sys_service.modules.tax_api.client.service.ServerInformationService;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.entity.general.SellerUser;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.Header;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.DataModel;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.PacketModel;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.RequestModel;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.res.server_info.ServerInfoPubKeyModel;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.res.server_info.ServerInfoResponseModel;
import com.cyancoder.tax_pay_sys_service.util.CryptoUtils;
import com.cyancoder.tax_pay_sys_service.util.Encryption;
import com.cyancoder.tax_pay_sys_service.util.KeyUtil;
import com.cyancoder.tax_pay_sys_service.util.SignText;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

@JsonFormat
@Data
public class InvoiceRequestModel  {

    private int time;
    private InvoicePacketModel packet;
    private String signature;


//    @Autowired
    ServerInformationService serverInformationService;

    public InvoiceRequestModel(Header header, String packetType, DataModel data, ServerInfoPubKeyModel serverKey, SellerUser seller) throws Exception {

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


        this.setDataSignature(data,seller);
        this.setSignature(header,seller);
    }


    protected void setSignature(Header header,SellerUser seller) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        String normalJsonStr = CryptoUtils.normalJson(packet, header);
        this.signature = SignText.getSignedText(normalJsonStr, "SHA256WITHRSA", KeyUtil.getPrivateKey(seller));

    }


    protected void setDataSignature(DataModel data,SellerUser seller) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        String normalJsonStr = CryptoUtils.normalJson(data, null);
        this.packet.setDataSignature(SignText.getSignedText(normalJsonStr, "SHA256WITHRSA", KeyUtil.getPrivateKey(seller)));

    }


}
