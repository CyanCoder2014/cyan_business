package com.cyancoder.taxpaysys.modules.tax_api.client.out_api.service;

import com.cyancoder.taxpaysys.modules.tax_api.client.out_api.auth.Token;
import com.cyancoder.taxpaysys.modules.tax_api.client.out_api.rest.AuthTaxClientController;
import com.cyancoder.taxpaysys.modules.tax_api.entity.general.SellerUser;
import com.cyancoder.taxpaysys.modules.tax_api.model.Header;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.auth.AuthRequestDataModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.RequestModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.auth.AuthResponseModel;
import com.cyancoder.taxpaysys.util.KeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthTaxClientController authTaxClientController;


    private String uniqueCode = null;
    private String privateKey = null;

    public AuthResponseModel getToken(String uniqueCode) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        Random rnd = new Random();
        Header header = new Header("2023-cyanbusiness-aut-2320011"+ rnd.nextInt(10));
        AuthRequestDataModel data = AuthRequestDataModel.builder().username(uniqueCode).build();
        RequestModel body = new RequestModel(header, "GET_TOKEN", data, privateKey);

        try {
            return new AuthResponseModel(authTaxClientController.getToken(header.getContentType(),header.getString("requestTraceId"),
                    header.getString("timestamp"),
                    body));
        } catch (Exception e) {
            return new AuthResponseModel(0, e.getMessage());
        }
    }


    public String setToken() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        AuthResponseModel authResponseModel = getToken(uniqueCode);
        log.warn("AuthService -> setToken authResponseModel: {}", authResponseModel.successResponse);
        Token
             .getInstance()
             .setToken(authResponseModel.successResponse != null ? authResponseModel.successResponse.result.data.token : "");

        return Token.getInstance().getToken();
    }



    public void setTokenInHeader(Header header, String uniqueCode, String privateKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        setCode(uniqueCode);
        setKey(privateKey);
        setToken();
        if (header != null)
            header.put("Authorization",Token.getInstance().getToken());
    }

    public String getCode() {
        return uniqueCode;
    }
 public String getKey() {
        return privateKey;
    }

    public void setCode(String uniqueCode) {
        this.uniqueCode = uniqueCode;
    }
    public void setKey(String privateKey) {
        this.privateKey = privateKey;
    }




}
