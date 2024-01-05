package com.cyancoder.taxpaysys.modules.home.dto.res;

import com.cyancoder.taxpaysys.modules.tax_api.model.Header;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.RequestModel;
import com.fasterxml.jackson.annotation.JsonFormat;

import javax.crypto.SecretKey;

@JsonFormat
public class TestEncryptResponseModel {

    public String serverKey;
    public SecretKey aesKey;
    public byte[] iv;
    public byte[] encrypt;
    public byte[] xor;




    public Header header;

    public RequestModel body;
    public String normalJsonStr;
    public String signatureStr;

    public TestEncryptResponseModel(

                                    String serverKey,
                                    SecretKey aesKey,
                                    byte[] iv,
                                    byte[] encrypt,
                                    byte[] xor
//                                    Header header,
//                                    RequestModel body,
//                                    String normalJsonStr,
//                                    String signatureStr
    ){
        this.serverKey = serverKey;
        this.aesKey = aesKey;
        this.iv = iv;
        this.encrypt = encrypt;
        this.xor = xor;


//        this.header = header;
//        this.body = body;
//        this.normalJsonStr = normalJsonStr;
//        this.signatureStr = signatureStr;

    }

}
