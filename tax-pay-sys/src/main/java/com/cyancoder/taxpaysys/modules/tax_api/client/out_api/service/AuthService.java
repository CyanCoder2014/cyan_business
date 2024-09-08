package com.cyancoder.taxpaysys.modules.tax_api.client.out_api.service;

import com.cyancoder.taxpaysys.modules.tax_api.client.out_api.auth.Token;
import com.cyancoder.taxpaysys.modules.tax_api.client.out_api.rest.AuthTaxClientController;
import com.cyancoder.taxpaysys.modules.tax_api.entity.general.SellerUser;
import com.cyancoder.taxpaysys.modules.tax_api.model.Header;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.auth.AuthRequestDataModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.RequestModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.auth.AuthResponseModel;
import com.cyancoder.taxpaysys.util.Encrypt;
import com.cyancoder.taxpaysys.util.KeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthTaxClientController authTaxClientController;

    private final StringRedisTemplate redisTemplate;

    private String uniqueCode = null;
    private String privateKey = null;

//    @Cacheable(cacheManager = "cacheManager", cacheNames = "default")
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
            throw new RuntimeException(e.getMessage());
        }
    }


    public String setToken() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        String encryptedToken = redisTemplate.opsForValue().get(uniqueCode);

        if (encryptedToken != null) {
            String decryptedToken = Encrypt.decrypt(encryptedToken,uniqueCode);
            return decryptedToken;
        }

        AuthResponseModel authResponseModel = getToken(uniqueCode);
        log.warn("AuthService -> setToken authResponseModel: {}", authResponseModel.successResponse);
        Token
                .getInstance()
                .setToken(authResponseModel.successResponse != null ? authResponseModel.successResponse.result.data.token : "");
        String newToken = Token.getInstance().getToken();

        String encryptedNewToken = Encrypt.encrypt(newToken,uniqueCode);
        redisTemplate.opsForValue().set(uniqueCode, encryptedNewToken, 60, TimeUnit.SECONDS);

        return newToken;
    }



    public void setTokenInHeader(Header header, String uniqueCode, String privateKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        setCode(uniqueCode);
        setKey(privateKey);
        setToken();
//        if (header != null) // need to consider !!!!!!!
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
