package main.java.com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.inquiry.inquiry_data.inquiry_by_uid;


import com.cyancoder.tax_pay_sys_service.modules.tax_api.entity.general.SellerUser;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.Header;
import com.cyancoder.tax_pay_sys_service.util.CryptoUtils;
import com.cyancoder.tax_pay_sys_service.util.KeyUtil;
import com.cyancoder.tax_pay_sys_service.util.SignText;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

@Data
@JsonFormat
public class InquiryByUidRequestModel {

    public InquiryByUidRequestModel(Header header, String packetType, InquiryByUidDataModel data, SellerUser seller) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        this.time = 1;
        this.packet = InquiryByUidPacketModel.builder()
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
    private InquiryByUidPacketModel packet;
    private String signature;

    protected void setSignature(Header header,SellerUser seller) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        String normalJsonStr = CryptoUtils.normalJson(packet, header);
        this.signature = SignText.getSignedText(normalJsonStr, "SHA256WITHRSA", KeyUtil.getPrivateKey(seller));

    }
}
