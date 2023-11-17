package com.cyancoder.taxpaysys.modules.tax_api.client.service;

import com.cyancoder.taxpaysys.modules.tax_api.client.auth.Token;
import com.cyancoder.taxpaysys.modules.tax_api.client.rest.AuthTaxClientController;
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


    private SellerUser seller = null;

    public AuthResponseModel getToken(SellerUser seller) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        log.warn("seller: {}",String.valueOf(seller));
        Random rnd = new Random();
        Header header = new Header("2023-cyanbusiness-aut-2320011"+ rnd.nextInt(10));
        AuthRequestDataModel data = AuthRequestDataModel.builder().username(KeyUtil.getClientId(seller)).build();
        RequestModel body = new RequestModel(header, "GET_TOKEN", data, seller);

        try {
            return new AuthResponseModel(authTaxClientController.getToken(header.getContentType(),header.getString("requestTraceId"),
                    header.getString("timestamp"),
                    body));
        } catch (Exception e) {
            return new AuthResponseModel(0, e.getMessage());
        }
    }


    public String setToken() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        AuthResponseModel authResponseModel = getToken(getSeller());
        log.warn("AuthService -> setToken authResponseModel: {}", authResponseModel.successResponse);
        Token
             .getInstance()
             .setToken(authResponseModel.successResponse != null ? authResponseModel.successResponse.result.data.token : "");

        return Token.getInstance().getToken();
    }



    public void setTokenInHeader(Header header, SellerUser seller) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        setSeller(seller);
        setToken();
        if (header != null)
            header.put("Authorization",Token.getInstance().getToken());
    }

    public SellerUser getSeller() {
        return seller;
    }

    public void setSeller(SellerUser seller) {
        this.seller = seller;
    }




}
